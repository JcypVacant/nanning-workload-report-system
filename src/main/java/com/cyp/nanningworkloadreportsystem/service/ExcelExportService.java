package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.entity.*;
import com.cyp.nanningworkloadreportsystem.mapper.*;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel导出服务类
 * 基于模板（南宁通信段劳动用工工时、工分统计表工区.xlsx）填充数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final WorkReportMapper reportMapper;
    private final WorkReportItemMapper itemMapper;
    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final WorkItemMapper workItemMapper;
    private final MonthlyPeriodMapper periodMapper;

    private static final String TEMPLATE_PATH = "templates/工区填报模板.xlsx";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d日");

    /** 用工项目路径 → 模板列索引(0-based) 的映射，在首次导出时延迟初始化 */
    private volatile Map<String, Integer> columnMapping = null;

    /**
     * 导出工区数据
     */
    public byte[] exportArea(Long periodId, Long areaId) {
        if (UserContext.isAreaReporter() && !UserContext.getOrgId().equals(areaId)) {
            throw new RuntimeException("无权导出其他工区的数据");
        }
        if (UserContext.isWorkshopAdmin()) {
            OrgUnit area = orgUnitMapper.selectById(areaId);
            if (area == null || !UserContext.getOrgId().equals(area.getParentId())) {
                throw new RuntimeException("无权导出其他车间下属工区的数据");
            }
        }

        // 查询该工区该月份所有已审核/已锁定的填报记录
        List<WorkReport> reports = reportMapper.selectList(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getPeriodId, periodId)
                        .eq(WorkReport::getAreaId, areaId)
                        .in(WorkReport::getStatus, Arrays.asList("已审核", "已锁定"))
                        .orderByAsc(WorkReport::getEmployeeId, WorkReport::getWorkDate));

        // 获取工区、车间信息
        OrgUnit area = orgUnitMapper.selectById(areaId);
        String areaName = area != null ? area.getOrgName() : "";
        String workshopName = "";
        if (area != null && area.getParentId() != null) {
            OrgUnit workshop = orgUnitMapper.selectById(area.getParentId());
            workshopName = workshop != null ? workshop.getOrgName() : "";
        }

        // 获取月份名称
        MonthlyPeriod period = periodMapper.selectById(periodId);
        String monthLabel = "";
        if (period != null && period.getPeriodName() != null) {
            monthLabel = period.getPeriodName().replace("年", "年").replace("月", "月度");
        }

        // 加载模板
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) throw new RuntimeException("模板文件不存在: " + TEMPLATE_PATH);
            Workbook wb = new XSSFWorkbook(is);
            Sheet sheet = wb.getSheetAt(0);

            // 移除数据区域已有的合并单元格（rows 9+），避免与新数据合并冲突
            List<Integer> toRemove = new ArrayList<>();
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                if (sheet.getMergedRegion(i).getFirstRow() >= 9) {
                    toRemove.add(i);
                }
            }
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                sheet.removeMergedRegion(toRemove.get(i));
            }

            // 填充标题
            fillTitle(sheet, workshopName, areaName, monthLabel);

            // 初始化列映射（首次调用）
            if (columnMapping == null) {
                synchronized (this) {
                    if (columnMapping == null) {
                        columnMapping = buildColumnMapping(sheet);
                    }
                }
            }

            // 加载所有用工项目
            List<WorkItem> allItems = workItemMapper.selectList(null);
            Map<Long, WorkItem> itemMap = allItems.stream()
                    .collect(Collectors.toMap(WorkItem::getId, i -> i));

            // 按人员分组
            Map<Long, Employee> empMap = loadEmployees(reports);

            // 按人员+日期+类别聚合：employeeId -> date -> type -> (itemPath -> sum)
            Map<Long, Map<String, Map<String, Map<String, BigDecimal>>>> empDateAgg = new LinkedHashMap<>();
            for (WorkReport report : reports) {
                Long empId = report.getEmployeeId();
                String date = report.getWorkDate() != null ? report.getWorkDate().format(DATE_FMT) : "";
                String type = report.getReportType();
                empDateAgg.computeIfAbsent(empId, k -> new LinkedHashMap<>());
                empDateAgg.get(empId).computeIfAbsent(date, k -> new LinkedHashMap<>());
                empDateAgg.get(empId).get(date).computeIfAbsent(type, k -> new LinkedHashMap<>());

                List<WorkReportItem> items = itemMapper.selectList(
                        new LambdaQueryWrapper<WorkReportItem>()
                                .eq(WorkReportItem::getReportId, report.getId()));
                for (WorkReportItem item : items) {
                    WorkItem wi = itemMap.get(item.getWorkItemId());
                    if (wi == null || wi.getItemPath() == null) continue;
                    String path = wi.getItemPath().trim();
                    BigDecimal val = "HOURS".equals(type)
                            ? (item.getNumberValue() != null ? item.getNumberValue() : BigDecimal.ZERO)
                            : (item.getPointsValue() != null ? item.getPointsValue() : BigDecimal.ZERO);
                    empDateAgg.get(empId).get(date).get(type).merge(path, val, BigDecimal::add);
                }
            }

            // 重新组织数据：按日期→人员排序
            // 先收集所有 (date, empId) 对，按日期排序再按人员姓名排序
            Map<String, Map<Long, Map<String, Map<String, BigDecimal>>>> dateEmpAgg = new LinkedHashMap<>();
            for (Map.Entry<Long, Map<String, Map<String, Map<String, BigDecimal>>>> empEntry : empDateAgg.entrySet()) {
                Long empId = empEntry.getKey();
                for (Map.Entry<String, Map<String, Map<String, BigDecimal>>> dateEntry : empEntry.getValue().entrySet()) {
                    String date = dateEntry.getKey();
                    dateEmpAgg.computeIfAbsent(date, k -> new LinkedHashMap<>())
                            .put(empId, dateEntry.getValue());
                }
            }

            // 按日期排序
            List<String> sortedDates = new ArrayList<>(dateEmpAgg.keySet());
            sortedDates.sort(Comparator.comparingInt(s -> {
                try { return Integer.parseInt(s.replace("日", "")); } catch (NumberFormatException e) { return 0; }
            }));

            // 填充数据行，按日期→人员
            int rowIdx = 9;
            BigDecimal grandTotalHours = BigDecimal.ZERO;
            BigDecimal grandTotalPoints = BigDecimal.ZERO;
            for (String date : sortedDates) {
                Map<Long, Map<String, Map<String, BigDecimal>>> empMap2 = dateEmpAgg.get(date);
                // 按人员姓名排序
                List<Long> sortedEmpIds = new ArrayList<>(empMap2.keySet());
                sortedEmpIds.sort(Comparator.comparing(eid -> {
                    Employee e = empMap.get(eid);
                    return e != null ? e.getName() : "";
                }));

                for (Long empId : sortedEmpIds) {
                    Employee emp = empMap.get(empId);
                    String empName = emp != null ? emp.getName() : "未知";
                    Map<String, Map<String, BigDecimal>> typeMap = empMap2.get(empId);

                    // 工时行
                    getOrCreateRow(sheet, rowIdx++);
                    BigDecimal hoursTotal = fillDataRow(sheet.getRow(rowIdx - 1), true, empName, date, "工时",
                            typeMap.getOrDefault("HOURS", Collections.emptyMap()));
                    grandTotalHours = grandTotalHours.add(hoursTotal);

                    // 工分行
                    getOrCreateRow(sheet, rowIdx++);
                    BigDecimal pointsTotal = fillDataRow(sheet.getRow(rowIdx - 1), false, null, null, "工分",
                            typeMap.getOrDefault("POINTS", Collections.emptyMap()));
                    grandTotalPoints = grandTotalPoints.add(pointsTotal);

                    // 合并A、B列（姓名+日期），C列不合并
                    mergeCellIfNeeded(sheet, rowIdx - 2, rowIdx - 1, 0);
                    mergeCellIfNeeded(sheet, rowIdx - 2, rowIdx - 1, 1);
                }
            }

            // 填入 DH8/DH9 合计（row 7/8, 0-based）
            Row row8 = getOrCreateRow(sheet, 7);
            setCell(row8, 111, grandTotalHours.doubleValue());
            Row row9 = getOrCreateRow(sheet, 8);
            setCell(row9, 111, grandTotalPoints.doubleValue());

            // 输出
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                wb.write(baos);
                return baos.toByteArray();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    /** 填充标题：替换模板中的 XX */
    private void fillTitle(Sheet sheet, String workshopName, String areaName, String monthLabel) {
        Row titleRow = sheet.getRow(0);
        if (titleRow == null) return;
        Cell titleCell = titleRow.getCell(1); // B1
        if (titleCell == null) return;
        String title = titleCell.getStringCellValue();
        if (workshopName != null) title = title.replace("XX车间", workshopName);
        if (areaName != null) title = title.replace("XX工区", areaName);
        if (monthLabel != null) title = title.replace("XX班组", monthLabel);
        titleCell.setCellValue(title);
    }

    /** 获取或创建行（新建行时从模板第一行数据行复制样式，避免超出行没有边框） */
    private Row getOrCreateRow(Sheet sheet, int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
            // 从模板第10行复制样式
            Row templateRow = sheet.getRow(9);
            if (templateRow != null) {
                for (int c = 0; c <= 111; c++) {
                    Cell tCell = templateRow.getCell(c);
                    if (tCell != null) {
                        Cell newCell = row.createCell(c);
                        newCell.setCellStyle(tCell.getCellStyle());
                    }
                }
            }
        }
        return row;
    }

    /** 填充一行数据，返回该行合计值 */
    private BigDecimal fillDataRow(Row row, boolean isFirstRow, String name, String dates, String type,
                             Map<String, BigDecimal> itemValues) {
        if (isFirstRow) {
            setCell(row, 0, name);   // A: 姓名（仅第一行）
            setCell(row, 1, dates);  // B: 日期（仅第一行）
        }
        setCell(row, 2, type);       // C: 工时/工分（每行都填）

        // 填入各用工项目数值
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : itemValues.entrySet()) {
            String path = entry.getKey();
            BigDecimal val = entry.getValue();
            Integer colIdx = findColumn(path);
            if (colIdx != null) {
                setCell(row, colIdx, val.doubleValue());
                total = total.add(val);
            } else {
                colIdx = fuzzyFindColumn(path);
                if (colIdx != null) {
                    setCell(row, colIdx, val.doubleValue());
                    total = total.add(val);
                }
            }
        }

        // 合计列（DH=111, 0-based=110）
        setCell(row, 111, total.doubleValue());
        return total;
    }

    /** 精确查找列 */
    private Integer findColumn(String itemPath) {
        return columnMapping.get(itemPath);
    }

    /** 模糊匹配：用path的各层级逐步匹配 */
    private Integer fuzzyFindColumn(String itemPath) {
        if (itemPath == null) return null;
        String[] parts = itemPath.split("/");
        // 从完整路径开始逐步缩短
        for (int len = parts.length; len >= 1; len--) {
            String key = String.join("/", Arrays.copyOf(parts, len)).trim();
            Integer col = columnMapping.get(key);
            if (col != null) return col;
        }
        return null;
    }

    /** 设置单元格值 */
    private void setCell(Row row, int colIdx, Object value) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        }
    }

    /** 合并单元格 */
    private void mergeCellIfNeeded(Sheet sheet, int row1, int row2, int col) {
        if (row1 == row2) return;
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(row1, row2, col, col));
    }

    /** 加载员工信息 */
    private Map<Long, Employee> loadEmployees(List<WorkReport> reports) {
        Set<Long> empIds = reports.stream().map(WorkReport::getEmployeeId).collect(Collectors.toSet());
        Map<Long, Employee> map = new HashMap<>();
        for (Long id : empIds) {
            Employee emp = employeeMapper.selectById(id);
            if (emp != null) map.put(id, emp);
        }
        return map;
    }

    /**
     * 构建用工项目路径 → 模板列索引的映射
     * 解析模板第2-6行的表头结构
     */
    private Map<String, Integer> buildColumnMapping(Sheet sheet) {
        Map<String, Integer> mapping = new LinkedHashMap<>();
        // 预加载合并区域列表
        List<org.apache.poi.ss.util.CellRangeAddress> mergedRegions = sheet.getMergedRegions();

        // 解析每列(row2+row3+row4+row5)拼接为完整路径
        for (int col = 3; col < 112; col++) { // D(3) to DH(111), 0-based
            List<String> pathParts = new ArrayList<>();
            for (int row = 1; row <= 5; row++) { // rows 2-6, 0-based=1-5
                String val = getCellValueFromMerged(sheet, mergedRegions, row, col);
                if (val != null && !val.isEmpty() && !val.startsWith("备注") && !val.startsWith("单位")) {
                    pathParts.add(val);
                }
            }
            if (!pathParts.isEmpty()) {
                // 去重连续重复值（合并单元格跨多行导致同一值被读多次）
                List<String> deduped = new ArrayList<>();
                for (String p : pathParts) {
                    if (deduped.isEmpty() || !deduped.get(deduped.size() - 1).equals(p)) {
                        deduped.add(p);
                    }
                }
                String path = String.join("/", deduped);
                mapping.put(path, col);
            }
        }

        log.info("已构建列映射: {} 个用工项目列", mapping.size());
        return mapping;
    }

    /** 获取单元格值，支持合并单元格（若单元格在合并区域内，取左上角值） */
    private String getCellValueFromMerged(Sheet sheet, List<org.apache.poi.ss.util.CellRangeAddress> mergedRegions,
                                           int row, int col) {
        // 先尝试直接读取
        Row r = sheet.getRow(row);
        if (r != null) {
            Cell cell = r.getCell(col);
            if (cell != null) {
                String val = getCellString(cell);
                if (val != null && !val.isEmpty()) return val;
            }
        }
        // 若为空，检查是否在合并区域中
        for (org.apache.poi.ss.util.CellRangeAddress region : mergedRegions) {
            if (region.isInRange(row, col)) {
                Cell topLeft = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
                if (topLeft != null) {
                    String val = getCellString(topLeft);
                    if (val != null && !val.isEmpty()) return val;
                }
            }
        }
        return null;
    }

    /** 获取单元格字符串值 */
    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    // ==================== 车间/段级导出（保留原有简单实现） ====================

    public byte[] exportWorkshop(Long periodId, Long workshopId) {
        if (UserContext.isWorkshopAdmin() && !UserContext.getOrgId().equals(workshopId)) {
            throw new RuntimeException("无权导出其他车间的数据");
        }
        return exportArea(periodId, workshopId); // 简化：导出车间=导出所有下属工区
    }

    public byte[] exportSection(Long periodId) {
        return doSimpleExport(periodId, null, null);
    }

    public byte[] exportSection(Long periodId, List<Long> workshopIds) {
        return doSimpleExport(periodId, null, null);
    }

    /** 简单导出（用于段级/车间级，避免模板复杂度） */
    private byte[] doSimpleExport(Long periodId, Long workshopId, Long areaId) {
        // 查询数据（同 exportArea 的逻辑）
        LambdaQueryWrapper<WorkReport> wrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(workshopId != null, WorkReport::getWorkshopId, workshopId)
                .eq(areaId != null, WorkReport::getAreaId, areaId)
                .in(WorkReport::getStatus, Arrays.asList("已审核", "已锁定"))
                .orderByAsc(WorkReport::getWorkshopId, WorkReport::getAreaId,
                        WorkReport::getEmployeeId, WorkReport::getWorkDate);

        if (UserContext.isWorkshopAdmin()) {
            wrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        } else if (UserContext.isAreaReporter()) {
            wrapper.eq(WorkReport::getAreaId, UserContext.getOrgId());
        }

        List<WorkReport> reports = reportMapper.selectList(wrapper);
        if (reports.isEmpty()) {
            try {
                return exportArea(periodId, areaId != null ? areaId : UserContext.getOrgId());
            } catch (Exception e) {
                return new byte[0];
            }
        }

        // 如果导出的是单个工区，使用模板导出
        if (areaId != null || (UserContext.isAreaReporter() && workshopId == null)) {
            return exportArea(periodId, areaId != null ? areaId : UserContext.getOrgId());
        }

        // 车间/段级暂时返回空（后续扩展）
        return new byte[0];
    }
}

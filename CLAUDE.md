# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

南宁通信段劳动用工工时工分统计管理系统 — enterprise work hours & work points reporting system with a 3-tier org hierarchy (Section → Workshop → Work Area) and RBAC.

## Repository Layout

```
nanning-workload-report-system/       ← Maven project root (Spring Boot 3 / JDK 21)
├── sql/                              ← 01_schema.sql, 02_init_data.sql, 03_seed.sql
├── src/main/java/com/cyp/nanningworkloadreportsystem/
│   ├── common/                       ← Result, PageResult, BaseEntity, GlobalExceptionHandler
│   ├── config/                       ← SaTokenConfig, MyBatisPlusConfig, WebMvcConfig
│   ├── controller/                   ← REST controllers
│   ├── dto/                          ← Request/response DTOs (SectionSummaryDTO, WorkshopSummaryDTO)
│   ├── entity/                       ← MyBatis Plus entities
│   ├── mapper/                       ← MyBatis Plus BaseMappers + custom SQL
│   ├── security/                     ← StpInterfaceImpl (role/permission loading), DataScope utility
│   ├── service/                      ← Business logic
│   └── util/                         ← UserContext (ThreadLocal-based)
└── nanning-workload-report-web/      ← Vue 3 frontend (separate npm project)
    └── src/
        ├── api/                      ← Axios API wrappers
        ├── router/                   ← Vue Router with role-based guards
        ├── store/                    ← Pinia stores (user, app)
        ├── types/                    ← TypeScript interfaces
        └── views/                    ← Page components organized by module
```

## Common Commands

### Backend (run from project root)
```bash
# Use Maven wrapper (required — system mvn 3.6.1 is too old)
./mvnw compile                    # Compile
./mvnw spring-boot:run            # Start on port 8080
./mvnw test                       # Run tests

# MySQL (database: nanning_workload, user: root, password: 1234)
mysql -u root -p1234 nanning_workload < sql/01_schema.sql
mysql -u root -p1234 nanning_workload < sql/02_init_data.sql
mysql -u root -p1234 nanning_workload < sql/03_seed.sql
```

### Frontend (run from `nanning-workload-report-web/` directory)
```bash
npm run dev                       # Dev server on port 5173, proxy /api → localhost:8080
npm run build                     # Production build (vue-tsc type-check + vite)
npx vite build                    # Vite-only build (skip type-check — use when vue-tsc has template-only errors)
```

### Git
```bash
git push                          # May need proxy: git config http.proxy http://127.0.0.1:7890
```

## Database

MySQL 8.x database: `nanning_workload`, charset `utf8mb4`. Credentials: `root` / `1234`.

11 tables: `org_unit`, `sys_user`, `sys_user_org_scope`, `employee`, `employee_transfer_record`, `work_item`, `monthly_period`, `work_report`, `work_report_item`, `audit_record`, `operation_log`.

**Schema quirks:**
- `work_report` uses `created_time`/`updated_time` (NOT `create_time`/`update_time` like other tables)
- `work_report_item.number_value` and `points_value` are `decimal(10,0)` (integers, was `decimal(10,2)`)
- `employee.area_id` is `bigint NULL` (allows workshop-level staff with no work area)
- `org_unit` has type `WORKSHOP_LEVEL` for workshop-level org units ("XX车间本级")
- Seed passwords are BCrypt-hashed `123456`

## Three Roles (RBAC)

| Role | Code | Data Scope |
|------|------|------------|
| 段级管理员 | `SECTION_ADMIN` | All data, all orgs |
| 车间管理员 | `WORKSHOP_ADMIN` | Own workshop + subordinate work areas |
| 工区填报员 | `AREA_REPORTER` | Own work area only |

Role permissions loaded via `StpInterfaceImpl` which queries `sys_user_org_scope`. Data scope enforced in Service layer via `UserContext.isXxxAdmin()` + manual WHERE conditions. Each user has exactly one role binding (`scope.get(0)`).

### Module access by role

| Module | SECTION_ADMIN | WORKSHOP_ADMIN | AREA_REPORTER |
|--------|:---:|:---:|:---:|
| Home dashboard | ✓ | ✓ | ✓ |
| Organization mgmt | ✓ | - | - |
| User mgmt | ✓ | - | - |
| Employee mgmt | ✓ | ✓(own workshop only) | - |
| Work item dictionary | ✓ | - | - |
| Period mgmt | ✓ | - | - |
| Area fill | - | - | ✓ |
| Workshop fill | - | ✓ | - |
| Workshop review | - | ✓ | - |
| Section review | ✓ | - | - |
| Audit records | ✓ | ✓ | ✓ |
| Summary mgmt | ✓ | ✓ | - |
| Statistics | ✓ | ✓ | ✓ |
| Excel export | ✓ | ✓ | ✓ |
| Progress monitor | ✓ | - | - |
| Operation logs | ✓ | - | - |
| Profile | ✓ | ✓ | ✓ |

## Complete Workflow

```
工区填报员提交 → status="已提交"
     ↓
车间管理员审核 (WorkshopReview.vue)
     ├─ 通过 → status="已审核"
     └─ 退回 → status="已退回"
     ↓
提交到段级 (submitToSection) → status="已锁定", period="段级汇总中"
     ↓
段级管理员汇总 (SummaryManage.vue) — 查看已锁定数据

车间本级数据路径：
车间管理员提交 → status="已提交"
     ↓
段级管理员审核 (SectionReview.vue)
     ├─ 通过 → status="已审核"
     └─ 退回 → status="已退回"
```

## Critical Technical Decisions & Gotchas

### MyBatis Plus 3.5.15 — No PaginationInnerInterceptor
`PaginationInnerInterceptor` was **removed** in MP 3.5.15. **Never use `selectPage()`**. Always use manual pattern:
```java
Long total = mapper.selectCount(countWrapper);
List<Entity> records = mapper.selectList(dataWrapper.last("LIMIT " + offset + ", " + pageSize));
Page<Entity> page = new Page<>(pageNum, pageSize);
page.setTotal(total);
page.setRecords(records);
```
Follow patterns in `SysUserService.getPage()`, `EmployeeService.getPage()`, `WorkReportService.getPage()`, and `AuditService.getAllReportsPage()`.

### BaseEntity Column Name Mismatch
`BaseEntity` maps `createTime`/`updateTime` → columns `create_time`/`update_time`. **Do NOT use BaseEntity** for `WorkReport` — its columns are `created_time`/`updated_time`. Define fields explicitly.

### BCrypt
Use Sa-Token's `cn.dev33.satoken.secure.BCrypt` — **not** Spring Security's BCrypt.

### Data Scope Pattern
Data scope in **Service layer**, never via SQL interceptor:
1. **Query**: `if (UserContext.isWorkshopAdmin()) wrapper.eq(Entity::getWorkshopId, UserContext.getOrgId())`
2. **Write guard**: Check ownership BEFORE status checks
3. **Controller**: Read from `UserContext.getOrgId()`, never trust client org IDs

### UserContext ThreadLocal
Cleaned up by `UserContextCleanupInterceptor` in `WebMvcConfig`.

### Frontend Patterns
- All tables use `el-pagination` with `v-model:current-page` + `v-model:page-size`, `:page-sizes="[10, 20, 50]"`, `@size-change` resets to page 1
- `el-date-picker` must have `value-format="YYYY-MM-DD"` to avoid timezone shift bugs (JS Date → UTC → LocalDate mismatch)
- `el-input-number` precision is `:precision="0"` (integer-only hours/points)
- Template type annotations (`(s: number, i: any)`) produce linter false positives — use `npx vite build` for verification
- `tsconfig.app.json`: `strict: false`, `skipLibCheck: true`

### File/Column patterns
- `work_report_item.item_path` uses `/` separator (e.g., `施工配合/路外/准备时间`)
- Time display: use `.replace('T', ' ')` for LocalDateTime format (ISO 8601 `T` separator)
- Account validation: username 5-20 chars, password 6-20 chars, login errors show unified "账号或密码错误"

### Excel Export
Uses POI to fill pre-formatted `.xlsx` template (`南宁通信段劳动用工工时、工分统计表工区.xlsx`). Three roles share same per-area export:
- Area reporter: auto-locked to own area
- Workshop admin: select area under own workshop
- Section admin: select workshop → area under it

Template has pre-merged cells in data rows (rows 10+). Before filling data, pre-existing merged regions are removed to avoid conflicts. Column mapping (`buildColumnMapping`) handles merged cells properly by reading top-left cell values and deduplicating consecutive identical values.

### Session fields
Login stores in Sa-Token session: `roleCode`, `scopeType`, `orgId`, `username`, `realName`, `orgName`. `orgName` is populated by looking up `org_unit` table via `OrgUnitMapper` in both `setUserInfoToSession()` and `getCurrentUser()`.

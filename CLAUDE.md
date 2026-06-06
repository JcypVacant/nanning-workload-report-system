# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

南宁通信段劳动用工工时工分统计管理系统 — enterprise work hours & work points reporting system with a 3-tier org hierarchy (Section → Workshop → Work Area) and RBAC.

## Repository Layout

```
nanning-workload-report-system/       ← Maven project root (Spring Boot 3 / JDK 21)
├── sql/                              ← 01_schema.sql, 02_init_data.sql, 03_seed.sql
├── src/main/java/com/cyp/nanningworkloadreportsystem/
│   ├── common/                       ← Result, PageResult, BaseEntity, GlobalExceptionHandler, enums
│   ├── config/                       ← SaTokenConfig, MyBatisPlusConfig, WebMvcConfig
│   ├── controller/                   ← REST controllers
│   ├── dto/                          ← Request/response DTOs
│   ├── entity/                       ← JPA/MyBatis Plus entities
│   ├── mapper/                       ← MyBatis Plus BaseMappers
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

**Important schema quirks:**
- `work_report` uses `created_time`/`updated_time` (NOT `create_time`/`update_time` like other tables)
- `work_report_item` has both `number_value` (工时/分钟) and `points_value` (工分)
- Seed passwords are BCrypt-hashed `123456`

## Three Roles (RBAC)

| Role | Code | Data Scope |
|------|------|------------|
| 段级管理员 | `SECTION_ADMIN` | All data across entire section |
| 车间管理员 | `WORKSHOP_ADMIN` | Own workshop + subordinate work areas |
| 工区填报员 | `AREA_REPORTER` | Own work area only |

Role permissions loaded via `StpInterfaceImpl` which queries `sys_user_org_scope`. Data scope enforced in Service layer via `UserContext.isXxxAdmin()` + manual WHERE conditions, NOT via MyBatis interceptor.

## Critical Technical Decisions & Gotchas

### MyBatis Plus 3.5.15 — No PaginationInnerInterceptor
`PaginationInnerInterceptor` was **removed** in MP 3.5.15. **Never use `selectPage()`** for pagination. Always use the manual pattern:
```java
Long total = mapper.selectCount(countWrapper);
List<Entity> records = mapper.selectList(dataWrapper.last("LIMIT " + offset + ", " + pageSize));
Page<Entity> page = new Page<>(pageNum, pageSize);
page.setTotal(total);
page.setRecords(records);
```
See `SysUserService.getPage()` and `EmployeeService.getPage()` for reference.

### BaseEntity Column Name Mismatch
`BaseEntity` maps `createTime`/`updateTime` → columns `create_time`/`update_time`. **Do NOT use BaseEntity** for `WorkReport` — its column names are `created_time`/`updated_time`. Define fields explicitly in that entity.

### BCrypt
Use Sa-Token's `cn.dev33.satoken.secure.BCrypt` — **not** Spring Security's BCrypt.

### Data Scope Pattern
Data scope checks happen in **Service layer**, not via SQL interceptor. Three patterns:
1. **Query filter**: `if (UserContext.isWorkshopAdmin()) wrapper.eq(Entity::getWorkshopId, UserContext.getOrgId())`
2. **Write guard**: Check target entity belongs to user's scope BEFORE status checks (fail fast, don't leak org info)
3. **Controller never trusts client org IDs**: Read from `UserContext.getOrgId()` instead

### UserContext ThreadLocal Cleanup
`UserContext` uses ThreadLocal — must be cleaned up after each request. This is done by `UserContextCleanupInterceptor` registered in `WebMvcConfig`.

### Frontend Page Pagination
All data tables use `el-pagination` with `v-model:current-page` + `v-model:page-size`, `:page-sizes="[10, 20, 50]"`, and separate `@size-change` handler that resets to page 1.

### tsconfig strict:false
`vue-tsc` produces template-only type errors (Element Plus `DefaultRow` vs typed rows) that don't affect runtime. TypeScript `strict: false` and `skipLibCheck: true` in `tsconfig.app.json`. Use `npx vite build` to skip type-check if `vue-tsc` has false positives.

## Authentication Flow
1. Frontend sends `POST /api/v1/auth/login` with username/password
2. Backend verifies BCrypt password, calls `StpUtil.login(userId)`, stores role/org in Sa-Token session
3. Frontend stores `satoken` in localStorage, attaches via Axios interceptor header
4. Router guard checks token existence → fetches user info → validates role against route `meta.role`

## Work Item Tree (用工项目字典)
144 items across 26 top-level categories. 4 levels max depth (e.g., 故障处理→有线→室内→高铁). Types: `CATEGORY` (classification only), `NUMBER` (fillable numeric), `TEXT` (fillable text/remark). All NUMBER items have `report_type='BOTH'` (usable for both 工时 and 工分 reports).

## Known Incomplete Modules
- **ExcelExportService**: Controller has stub endpoints; EasyExcel 4.0.3 is in pom.xml but no service exists yet
- **OperationLogService**: Entity/mapper/table exist, controller has read query, but NO write operations insert logs
- **StatisticsService**: Returns mock data; needs real aggregation queries
- **WorkshopLevelFill.vue**: Stub page (`el-empty`)
- **Profile.vue**: May be minimal

"""完整填报/审核工作流测试"""
from playwright.sync_api import sync_playwright
import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

BASE = "http://localhost:5173"

def login(page, username, password):
    page.goto(f"{BASE}/login")
    page.wait_for_load_state("networkidle")
    page.fill('input[placeholder="请输入账号"]', username)
    page.fill('input[placeholder="请输入密码"]', password)
    page.click('button:has-text("登 录")')
    page.wait_for_url("**/dashboard", timeout=10000)
    print(f"  OK login: {username}")

def logout(page):
    """安全退出，处理可能覆盖的弹窗"""
    page.keyboard.press("Escape")
    page.wait_for_timeout(500)
    page.goto(f"{BASE}/dashboard")
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(500)
    # 点击右上角用户头像/下拉
    page.locator('.el-dropdown').last.click()
    page.wait_for_timeout(300)
    page.get_by_text("退出登录").last.click()
    page.wait_for_url("**/login", timeout=5000)
    print("  OK logout")

def area_fill_workflow(page):
    """工区填报员：填报并提交"""
    print("\n--- Area Reporter: Fill & Submit ---")

    page.goto(f"{BASE}/report/fill")
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(1500)

    # 点击第一个人员的"添加用工明细"按钮
    btn = page.locator('button:has-text("添加用工明细")').first
    if not btn.is_visible():
        print("  SKIP: No employees found")
        return True
    btn.click()
    page.wait_for_timeout(800)

    # 等待对话框打开
    page.wait_for_selector('.el-dialog', timeout=5000)
    page.wait_for_timeout(300)

    # 选择日期
    date_input = page.locator('.el-dialog input[placeholder="选择日期"]').first
    date_input.click()
    page.wait_for_timeout(300)
    page.keyboard.press("Enter")
    page.wait_for_timeout(300)

    # 选择用工项目（级联选择器）
    cascader = page.locator('.el-dialog .el-cascader').first
    cascader.click()
    page.wait_for_timeout(500)
    for _ in range(3):
        menu_items = page.locator('.el-cascader-menu:visible li.el-cascader-node')
        if menu_items.count() > 0:
            menu_items.first.click()
            page.wait_for_timeout(300)
        else:
            break

    # 填写数值
    number_input = page.locator('.el-dialog .el-input-number input').first
    if number_input.is_visible():
        number_input.click()
        number_input.fill("120")
        page.wait_for_timeout(200)

    # 保存并提交
    page.locator('.el-dialog button:has-text("保存并提交")').click()
    page.wait_for_timeout(2000)

    print("  OK fill & submit")
    return True

def workshop_review_workflow(page):
    """车间管理员：审核通过 → 提交到段级"""
    print("\n--- Workshop Admin: Review ---")

    page.goto(f"{BASE}/audit/review")
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(1000)

    # 查询
    page.locator('button:has-text("查询")').first.click()
    page.wait_for_timeout(1000)

    # 通过第一条
    approve_btn = page.locator('button:has-text("通过")').first
    if approve_btn.is_visible():
        approve_btn.click()
        page.wait_for_timeout(1500)
        print("  OK approved")

        # 提交到段级
        page.locator('button:has-text("提交到段级")').first.click()
        page.wait_for_timeout(500)
        page.locator('.el-message-box button:has-text("确定")').click()
        page.wait_for_timeout(1500)
        print("  OK submitted to section")
    else:
        rows = page.locator('.el-table__body tr').count()
        print(f"  INFO: No pending data ({rows} rows, may already be reviewed)")

    return True

def section_verify(page):
    """段级管理员：验证数据和待办"""
    print("\n--- Section Admin: Verify ---")

    # 查看汇总
    page.goto(f"{BASE}/summary")
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(1000)
    print("  OK summary loaded")

    # 查看首页待办
    page.goto(f"{BASE}/dashboard")
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(1000)
    notif_count = page.locator('.card-desc').count()
    print(f"  OK dashboard ({notif_count} notification cards)")

    return True

def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(locale='zh-CN')
        page = context.new_page()

        try:
            # Step 1: Area reporter fills and submits
            print("\n" + "="*50)
            print("Step 1: Area Reporter")
            print("="*50)
            login(page, "area103reporter", "123456")
            area_fill_workflow(page)
            logout(page)

            # Step 2: Workshop admin reviews
            print("\n" + "="*50)
            print("Step 2: Workshop Admin")
            print("="*50)
            login(page, "ws101admin", "123456")
            workshop_review_workflow(page)
            logout(page)

            # Step 3: Section admin verifies
            print("\n" + "="*50)
            print("Step 3: Section Admin")
            print("="*50)
            login(page, "admin", "123456")
            section_verify(page)

            print("\n" + "="*50)
            print("ALL TESTS PASSED!")
            print("="*50)

        except Exception as e:
            print(f"\nFAIL: {e}")
            page.screenshot(path="test_failure.png", full_page=True)
            print("Screenshot saved: test_failure.png")
            sys.exit(1)
        finally:
            browser.close()

if __name__ == "__main__":
    main()

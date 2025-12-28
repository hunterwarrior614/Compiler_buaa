import os
import shutil
import subprocess
import difflib
import time
import signal
import threading

class MIPSTester:
    def __init__(self):
        self.test_base = "./2025代码生成公共测试程序库/"
        self.compiler_dir = "./"
        self.compiler_jar = "./out/artifacts/Compiler_buaa_jar/Compiler_buaa.jar"
        self.mars_jar = "./MARS2025+.jar"
        self.input_file = "./testfile.txt"
        self.mips_file = "./mips.txt"

        # MARS输出文件
        self.mars_output = "./mars_output.txt"

        # 测试结果统计
        self.results = {
            'A': {'total': 0, 'passed': 0, 'failed': 0, 'timeout': 0},
            'B': {'total': 0, 'passed': 0, 'failed': 0, 'timeout': 0},
            'C': {'total': 0, 'passed': 0, 'failed': 0, 'timeout': 0}
        }

        # 全局超时标志
        self.timeout_occurred = False

    def check_jar_exists(self):
        """检查编译器和MARS JAR包是否存在"""
        if not os.path.exists(self.compiler_jar):
            print(f"❌ 编译器JAR包不存在: {self.compiler_jar}")
            return False

        if not os.path.exists(self.mars_jar):
            print(f"❌ MARS JAR包不存在: {self.mars_jar}")
            return False

        return True

    def run_compiler(self):
        """运行编译器生成MIPS代码"""
        try:
            result = subprocess.run(
                ['java', '-jar', self.compiler_jar],
                cwd=self.compiler_dir,
                capture_output=True,
                text=True,
                timeout=3  # 编译器超时3秒
            )

            # 检查编译器是否成功生成MIPS代码
            if result.returncode != 0:
                print(f"编译器运行失败，返回码: {result.returncode}")
                if result.stderr:
                    print(f"错误信息: {result.stderr[:500]}")
                return False

            return True
        except subprocess.TimeoutExpired:
            print("编译器运行超时")
            return False
        except Exception as e:
            print(f"编译器运行异常: {e}")
            return False

    def run_mars(self, input_file=None):
        """运行MARS执行MIPS代码"""
        try:
            # 检查MIPS文件是否存在
            if not os.path.exists(self.mips_file):
                print(f"❌ MIPS文件不存在: {self.mips_file}")
                return False, ""

            # 构建MARS命令
            mars_cmd = ['java', '-jar', self.mars_jar, 'nc', self.mips_file]

            if input_file and os.path.exists(input_file):
                # 如果有输入文件，重定向输入
                with open(input_file, 'r') as f_in:
                    result = subprocess.run(
                        mars_cmd,
                        stdin=f_in,
                        capture_output=True,
                        text=True,
                        timeout=3  # MARS运行超时3秒
                    )
            else:
                # 没有输入文件
                result = subprocess.run(
                    mars_cmd,
                    capture_output=True,
                    text=True,
                    timeout=3
                )

            # 检查MARS是否成功运行
            if result.returncode != 0:
                print(f"MARS运行失败，返回码: {result.returncode}")
                if result.stderr:
                    print(f"错误信息: {result.stderr[:500]}")

                # 保存有问题的MIPS代码用于调试
                debug_mips = f"./debug_mips_{int(time.time())}.asm"
                shutil.copy(self.mips_file, debug_mips)
                print(f"有问题的MIPS代码已保存到: {debug_mips}")

                return False, ""

            # 返回程序输出
            return True, result.stdout

        except subprocess.TimeoutExpired:
            print("MARS运行超时")
            return False, ""
        except Exception as e:
            print(f"MARS运行异常: {e}")
            return False, ""

    def clean_output_files(self):
        """清理输出文件"""
        files_to_clean = [
            self.mips_file,
            self.mars_output,
            self.input_file
        ]

        for file_path in files_to_clean:
            if os.path.exists(file_path):
                os.remove(file_path)

    def compare_output(self, actual_output, expected_file):
        """比较程序输出与期望输出"""
        try:
            with open(expected_file, 'r', encoding='utf-8') as f:
                expected_output = f.read().strip()

            actual_clean = actual_output.strip()
            expected_clean = expected_output.strip()

            return actual_clean == expected_clean, actual_clean, expected_clean
        except Exception as e:
            print(f"文件比较错误: {e}")
            return False, "", ""

    def show_diff(self, actual, expected, testcase_path):
        """显示差异"""
        print(f"\n❌ 测试用例 {testcase_path} 输出不一致:")
        print("=" * 60)

        actual_lines = actual.split('\n')
        expected_lines = expected.split('\n')

        diff = difflib.unified_diff(
            expected_lines, actual_lines,
            fromfile='期望输出', tofile='实际输出',
            lineterm=''
        )

        diff_found = False
        for line in diff:
            diff_found = True
            if line.startswith('+'):
                print(f"\033[92m{line}\033[0m")  # 绿色显示新增
            elif line.startswith('-'):
                print(f"\033[91m{line}\033[0m")  # 红色显示删除
            elif line.startswith('@'):
                print(f"\033[94m{line}\033[0m")  # 蓝色显示位置
            else:
                print(line)

        if not diff_found:
            print("无差异显示（可能是空白字符差异）")

        print("=" * 60)

    def run_testcase_with_timeout(self, category, testcase_folder, timeout=5):
        """运行单个测试用例，带有总时间限制"""
        def _run():
            return self._run_testcase_internal(category, testcase_folder)

        # 创建线程运行测试
        result = [None]
        def target():
            result[0] = _run()

        thread = threading.Thread(target=target)
        thread.start()
        thread.join(timeout)

        if thread.is_alive():
            # 超时发生
            print(f" ⏰ 测试超时（超过{timeout}秒），强制结束")
            # 尝试终止可能的子进程
            self.force_terminate_processes()
            return False

        return result[0]

    def force_terminate_processes(self):
        """强制终止可能的Java进程"""
        try:
            if os.name == 'nt':  # Windows
                os.system('taskkill /F /IM java.exe 2>nul 1>nul')
            else:  # Linux/Mac
                os.system('pkill -9 java 2>/dev/null')
        except:
            pass

    def _run_testcase_internal(self, category, testcase_folder):
        """运行单个测试用例的内部实现"""
        testcase_path = os.path.join(self.test_base, category, testcase_folder)
        source_file = os.path.join(testcase_path, "testfile.txt")
        input_file = os.path.join(testcase_path, "in.txt")
        answer_file = os.path.join(testcase_path, "ans.txt")

        if not os.path.exists(source_file) or not os.path.exists(answer_file):
            print(f"⚠️  测试用例 {testcase_path} 文件不完整，跳过")
            return False

        # 清理之前的输出
        self.clean_output_files()

        # 复制测试文件
        shutil.copy(source_file, self.input_file)

        # 运行编译器生成MIPS代码
        print(f"   正在编译 {testcase_folder}...", end="")
        if not self.run_compiler():
            print(" ❌ 编译失败")
            return False
        print(" ✅")

        # 检查MIPS文件是否存在
        if not os.path.exists(self.mips_file):
            print(f"   测试用例 {testcase_path} 未生成MIPS文件")
            return False

        # 运行MARS执行MIPS代码
        print(f"   正在运行MIPS...", end="")
        input_file_to_use = input_file if os.path.exists(input_file) else None
        success, actual_output = self.run_mars(input_file_to_use)

        if not success:
            print(" ❌ 运行失败")
            return False
        print(" ✅")

        # 比较结果
        is_match, actual_clean, expected_clean = self.compare_output(actual_output, answer_file)

        if is_match:
            print(f"   通过测试")
            return True
        else:
            print(f"   测试失败")
            self.show_diff(actual_clean, expected_clean, f"{category}/{testcase_folder}")
            return False

    def run_testcase(self, category, testcase_folder):
        """运行单个测试用例（外部接口）"""
        return self.run_testcase_with_timeout(category, testcase_folder, timeout=5)

    def run_all_tests(self):
        """运行所有测试"""
        print("🚀 开始测试编译器MIPS代码生成...")
        print("=" * 60)

        if not self.check_jar_exists():
            print("❌ JAR包检查失败，终止测试")
            return

        print("✅ 所有JAR包存在，开始测试...\n")
        print(f"📌 每个测试样例限制在5秒内完成\n")

        # 遍历所有测试类别
        for category in ['A', 'B', 'C']:
            category_path = os.path.join(self.test_base, category)
            if not os.path.exists(category_path):
                print(f"⚠️  类别 {category} 不存在，跳过")
                continue

            print(f"\n📁 测试类别: {category}")
            print("-" * 40)

            # 获取所有测试用例文件夹
            testcases = [d for d in os.listdir(category_path)
                         if os.path.isdir(os.path.join(category_path, d))]
            testcases.sort()  # 按顺序测试

            for testcase in testcases:
                print(f"测试用例: {testcase}")
                self.results[category]['total'] += 1

                # 记录开始时间
                start_time = time.time()
                result = self.run_testcase(category, testcase)
                elapsed_time = time.time() - start_time

                # 检查是否超时
                if elapsed_time > 5:
                    print(f"   实际用时: {elapsed_time:.2f}秒 (超时)")
                    self.results[category]['timeout'] += 1
                    self.results[category]['failed'] += 1
                elif result:
                    print(f"   实际用时: {elapsed_time:.2f}秒")
                    self.results[category]['passed'] += 1
                else:
                    print(f"   实际用时: {elapsed_time:.2f}秒")
                    self.results[category]['failed'] += 1

                print("-" * 30)

        self.print_summary()

    def print_summary(self):
        """打印测试总结"""
        print("\n" + "=" * 60)
        print("📊 测试结果总结")
        print("=" * 60)

        total_all = 0
        passed_all = 0
        failed_all = 0
        timeout_all = 0

        for category in ['A', 'B', 'C']:
            stats = self.results[category]
            total = stats['total']
            passed = stats['passed']
            failed = stats['failed']
            timeout = stats['timeout']

            total_all += total
            passed_all += passed
            failed_all += failed
            timeout_all += timeout

            if total > 0:
                rate = (passed / total) * 100
                print(f"{category:6} | 通过: {passed:2d} | 失败: {failed:2d} | 超时: {timeout:2d} | 总计: {total:2d} | 通过率: {rate:6.2f}%")
            else:
                print(f"{category:6} | 无测试用例")

        if total_all > 0:
            overall_rate = (passed_all / total_all) * 100
            print("-" * 60)
            print(f"总计   | 通过: {passed_all:2d} | 失败: {failed_all:2d} | 超时: {timeout_all:2d} | 总计: {total_all:2d} | 通过率: {overall_rate:6.2f}%")

        print("=" * 60)

        # 给出建议
        if failed_all > 0 or timeout_all > 0:
            print("\n💡 建议:")
            if timeout_all > 0:
                print("1. 检查超时的测试用例，可能是死循环或性能问题")
            if failed_all - timeout_all > 0:
                print("2. 检查失败的测试用例，查看差异输出")
            print("3. 调试MIPS代码，可以使用MARS单步执行")
            print("4. 确保编译器正确处理了所有语法结构")

def main():
    # 检查必要目录是否存在
    if not os.path.exists("./2025代码生成公共测试程序库/"):
        print("❌ 测试程序库路径 './2025代码生成公共测试程序库/' 不存在")
        print("💡 请确保测试程序库在当前目录下")
        return

    tester = MIPSTester()
    tester.run_all_tests()

if __name__ == "__main__":
    main()
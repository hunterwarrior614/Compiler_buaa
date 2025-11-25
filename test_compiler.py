import os
import shutil
import subprocess
import difflib

class CompilerTester:
    def __init__(self):
        self.test_base = "./2025代码生成公共测试程序库/"
        self.compiler_dir = "./"
        self.compiler_jar = "./out/artifacts/Compiler_buaa_jar/Compiler_buaa.jar"
        self.input_file = "./testfile.txt"
        self.llvm_ir_file = "./llvm_ir.txt"
        self.linked_ir_file = "./out.ll"
        self.lib_file = "./lib.ll"
        self.runtime_output = "./runtime_output.txt"

        # 测试结果统计
        self.results = {
            'A': {'total': 0, 'passed': 0, 'failed': 0},
            'B': {'total': 0, 'passed': 0, 'failed': 0},
            'C': {'total': 0, 'passed': 0, 'failed': 0}
        }

    def check_jar_exists(self):
        """检查JAR包是否存在"""
        if not os.path.exists(self.compiler_jar):
            print(f"❌ JAR包不存在: {self.compiler_jar}")
            return False
        return True

    def check_lib_exists(self):
        """检查lib.ll是否存在"""
        if not os.path.exists(self.lib_file):
            print(f"❌ lib.ll文件不存在: {self.lib_file}")
            return False
        return True

    def run_compiler(self):
        """运行编译器JAR包生成LLVM IR"""
        try:
            result = subprocess.run(
                ['java', '-jar', self.compiler_jar],
                cwd=self.compiler_dir,
                capture_output=True,
                text=True,
                timeout=10  # 10秒超时
            )
            # 注意：这里我们只检查程序是否正常启动，不检查返回码
            # 因为编译器可能在遇到错误时返回非0值，但这在测试中是正常的
            return True
        except subprocess.TimeoutExpired:
            print("程序运行超时")
            return False
        except Exception as e:
            print(f"运行异常: {e}")
            return False

    def link_and_run_llvm(self, testcase_input=None):
        """链接LLVM IR并运行程序"""
        try:
            # 第一步：链接LLVM IR
            link_cmd = ['llvm-link', self.llvm_ir_file, self.lib_file, '-S', '-o', self.linked_ir_file]
            link_result = subprocess.run(
                link_cmd,
                capture_output=True,
                text=True,
                timeout=10
            )

            if link_result.returncode != 0:
                print(f"LLVM链接失败: {link_result.stderr}")
                return False, ""

            # 第二步：运行程序
            run_cmd = ['lli', self.linked_ir_file]

            if testcase_input and os.path.exists(testcase_input):
                # 如果有输入文件，重定向输入
                with open(testcase_input, 'r') as f_in:
                    run_result = subprocess.run(
                        run_cmd,
                        stdin=f_in,
                        capture_output=True,
                        text=True,
                        timeout=10
                    )
            else:
                # 没有输入文件
                run_result = subprocess.run(
                    run_cmd,
                    capture_output=True,
                    text=True,
                    timeout=10
                )

            if run_result.returncode != 0:
                print(f"程序运行失败: {run_result.stderr}")
                return False, ""

            # 返回程序输出
            return True, run_result.stdout

        except subprocess.TimeoutExpired:
            print("LLVM链接或运行超时")
            return False, ""
        except Exception as e:
            print(f"LLVM处理异常: {e}")
            return False, ""

    def clean_output_files(self):
        """清理输出文件"""
        files_to_clean = [
            self.llvm_ir_file,
            self.linked_ir_file,
            self.runtime_output,
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
        print("=" * 50)

        actual_lines = actual.split('\n')
        expected_lines = expected.split('\n')

        diff = difflib.unified_diff(
            expected_lines, actual_lines,
            fromfile='期望输出', tofile='实际输出',
            lineterm=''
        )

        for line in diff:
            if line.startswith('+'):
                print(f"\033[92m{line}\033[0m")  # 绿色显示新增
            elif line.startswith('-'):
                print(f"\033[91m{line}\033[0m")  # 红色显示删除
            else:
                print(line)
        print("=" * 50)

    def run_testcase(self, category, testcase_folder):
        """运行单个测试用例"""
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

        # 运行编译器生成LLVM IR
        if not self.run_compiler():
            return False

        # 检查LLVM IR文件是否存在
        if not os.path.exists(self.llvm_ir_file):
            print(f"❌ 测试用例 {testcase_path} 未生成LLVM IR文件")
            return False

        # 链接并运行LLVM IR
        input_file_to_use = input_file if os.path.exists(input_file) else None
        success, actual_output = self.link_and_run_llvm(input_file_to_use)

        if not success:
            return False

        # 比较结果
        is_match, actual_clean, expected_clean = self.compare_output(actual_output, answer_file)

        if is_match:
            print(f"✅ {category}/{testcase_folder} 通过")
            return True
        else:
            print(f"❌ {category}/{testcase_folder} 失败")
            self.show_diff(actual_clean, expected_clean, f"{category}/{testcase_folder}")
            return False

    def run_all_tests(self):
        """运行所有测试"""
        print("🚀 开始测试编译器代码生成...")
        if not self.check_jar_exists():
            print("❌ JAR包检查失败，终止测试")
            return

        if not self.check_lib_exists():
            print("❌ lib.ll检查失败，终止测试")
            return

        print("✅ JAR包和lib.ll存在，开始测试...\n")

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
                self.results[category]['total'] += 1
                if self.run_testcase(category, testcase):
                    self.results[category]['passed'] += 1
                else:
                    self.results[category]['failed'] += 1

        self.print_summary()

    def print_summary(self):
        """打印测试总结"""
        print("\n" + "=" * 60)
        print("📊 测试结果总结")
        print("=" * 60)

        total_all = 0
        passed_all = 0

        for category in ['A', 'B', 'C']:
            stats = self.results[category]
            total = stats['total']
            passed = stats['passed']
            failed = stats['failed']

            total_all += total
            passed_all += passed

            if total > 0:
                rate = (passed / total) * 100
                print(f"{category:6} | 通过: {passed:2d} | 失败: {failed:2d} | 总计: {total:2d} | 通过率: {rate:6.2f}%")
            else:
                print(f"{category:6} | 无测试用例")

        if total_all > 0:
            overall_rate = (passed_all / total_all) * 100
            print("-" * 60)
            print(f"总计   | 通过: {passed_all:2d} | 失败: {total_all-passed_all:2d} | 总计: {total_all:2d} | 通过率: {overall_rate:6.2f}%")

        print("=" * 60)

def main():
    # 检查必要目录是否存在
    if not os.path.exists("./2025代码生成公共测试程序库/"):
        print("❌ 测试程序库路径 './2025代码生成公共测试程序库/' 不存在")
        return

    tester = CompilerTester()
    tester.run_all_tests()

if __name__ == "__main__":
    main()
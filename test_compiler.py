import os
import shutil
import subprocess
import difflib

class CompilerTester:
    def __init__(self):
        self.test_base = "../2025语义分析公共测试程序库/"
        self.compiler_dir = "./"
        self.compiler_jar = "./out/artifacts/Compiler_buaa_jar/Compiler_buaa.jar"
        self.input_file = "./testfile.txt"
        self.output_symbol = "./symbol.txt"
        self.output_error = "./error.txt"

        # 测试结果统计
        self.results = {
            'A': {'total': 0, 'passed': 0, 'failed': 0},
            'B': {'total': 0, 'passed': 0, 'failed': 0},
            'C': {'total': 0, 'passed': 0, 'failed': 0},
            'error': {'total': 0, 'passed': 0, 'failed': 0}
        }

    def check_jar_exists(self):
        """检查JAR包是否存在"""
        if not os.path.exists(self.compiler_jar):
            print(f"❌ JAR包不存在: {self.compiler_jar}")
            return False
        return True

    def run_compiler(self):
        """运行编译器JAR包"""
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

    def clean_output_files(self):
        """清理输出文件"""
        for file_path in [self.output_symbol, self.output_error]:
            if os.path.exists(file_path):
                os.remove(file_path)

    def compare_files(self, file1, file2):
        """比较两个文件内容是否相同"""
        try:
            with open(file1, 'r', encoding='utf-8') as f1, \
                 open(file2, 'r', encoding='utf-8') as f2:
                content1 = f1.read().strip()
                content2 = f2.read().strip()
                return content1 == content2, content1, content2
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
        answer_file = os.path.join(testcase_path, "ans.txt")

        if not os.path.exists(source_file) or not os.path.exists(answer_file):
            print(f"⚠️  测试用例 {testcase_path} 文件不完整，跳过")
            return False

        # 清理之前的输出
        self.clean_output_files()

        # 复制测试文件
        shutil.copy(source_file, self.input_file)

        # 运行编译器
        if not self.run_compiler():
            return False

        # 确定输出文件
        if category == 'error':
            output_file = self.output_error
        else:
            output_file = self.output_symbol

        # 检查输出文件是否存在
        if not os.path.exists(output_file):
            print(f"❌ 测试用例 {testcase_path} 无输出文件")
            return False

        # 比较结果
        is_match, actual_output, expected_output = self.compare_files(output_file, answer_file)

        if is_match:
            print(f"✅ {category}/{testcase_folder} 通过")
            return True
        else:
            print(f"❌ {category}/{testcase_folder} 失败")
            self.show_diff(actual_output, expected_output, f"{category}/{testcase_folder}")
            return False

    def run_all_tests(self):
        """运行所有测试"""
        print("🚀 开始测试编译器...")
        if not self.check_jar_exists():
            print("❌ JAR包检查失败，终止测试")
            return

        print("✅ JAR包存在，开始测试...\n")

        # 遍历所有测试类别
        for category in ['A', 'B', 'C', 'error']:
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

        for category in ['A', 'B', 'C', 'error']:
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
    if not os.path.exists("../2025语义分析公共测试程序库/"):
        print("❌ 测试程序库路径 '../2025语义分析公共测试程序库/' 不存在")
        return

    # 不再检查Compiler.java，而是检查JAR包
    tester = CompilerTester()
    tester.run_all_tests()

if __name__ == "__main__":
    main()
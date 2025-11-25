#!/bin/bash

# 简化版LLVM链接运行脚本
set -e  # 遇到错误立即退出

echo "开始LLVM IR链接..."
llvm-link llvm_ir.txt lib.ll -S -o out.ll

echo "链接完成，运行程序..."
lli out.ll
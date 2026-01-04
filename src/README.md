1. 下载 lib.ll

   执行LLVM IR文件

   ```bash
   # 使用 llvm-link 将两个文件链接，生成新的 IR 文件
   llvm-link llvm_ir.txt lib.ll -S -o out.ll
   
   # 用 lli 解释运行
   lli out.ll
   ```

2. gitignore

   ```
   out/
   .idea/
   *.iml
   *.txt
   *.zip
   META-INF/
   out.ll
   libsysy
   ```

3. 下载两个两个版本的编译器


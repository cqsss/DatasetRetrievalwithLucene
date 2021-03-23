
- Bug
`ImportError: DLL load failed while importing _swigfaiss: 找不到指定的模块。`
- Solution
卸载faiss后重新安装
`conda install -c conda-forge faiss`

- Bug
`AssertionError: Torch not compiled with CUDA enabled`
- Solution
删除'.cuda()'

- Bug
`RuntimeError: [enforce fail at ..\c10\core\CPUAllocator.cpp:72] data. DefaultCPUAllocator: not enough memory: you tried to allocate 25165824 bytes. Buy new RAM!`
- Solution


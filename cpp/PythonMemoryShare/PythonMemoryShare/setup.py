from distutils.core import setup, Extension

pymodule = Extension('sharedmemory', sources=['dllmain.cpp'])

setup(name = 'sharedmemory', version = '1.0', description = 'Adds the ability to access shared memory on windows', ext_modules = [pymodule])
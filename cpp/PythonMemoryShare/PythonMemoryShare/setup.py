from distutils.core import setup, Extension

pymodule = Extension('SharedMemory', sources=['dllmain.cpp'])

setup(name = 'SharedMemory', version = '1.0', description = 'Adds the ability to access shared memory on windows', ext_modules = [pymodule])
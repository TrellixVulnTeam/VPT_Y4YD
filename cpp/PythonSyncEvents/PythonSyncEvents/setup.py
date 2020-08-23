from distutils.core import setup, Extension

pymodule = Extension('SyncEvents', sources=['dllmain.cpp'])

setup(name = 'SyncEvents', version = '1.0', description = 'Adds access to windows synchronization features', ext_modules = [pymodule])
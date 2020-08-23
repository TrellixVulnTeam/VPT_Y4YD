from distutils.core import setup, Extension

pymodule = Extension('syncevents', sources=['dllmain.cpp'])

setup(name = 'syncevents', version = '1.0', description = 'Adds access to windows synchronization features', ext_modules = [pymodule])
from sharedmemory import *

handle = OpenSharedMemory("LOCAL\test")
input('')
print(ReadSharedMemory(handle))
CloseSharedMemory(handle)
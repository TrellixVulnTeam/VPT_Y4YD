from sharedmemory import *

handle2 = OpenSharedMemory("LOCAL\test")
input('')
print(ReadSharedMemory(handle2))
CloseSharedMemory(handle2)
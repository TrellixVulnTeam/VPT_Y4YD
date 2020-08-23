from SharedMemory import *

handle2 = OpenSharedMemory("Local\\test")
input('')
print(ReadSharedMemory(handle2))
CloseSharedMemory(handle2)
from syncevents import *

mutex = CreateMutex('LOCAL\test')
WaitForMutex(mutex)
print('test')
ReleaseMutex(mutex)
CloseMutex(mutex)
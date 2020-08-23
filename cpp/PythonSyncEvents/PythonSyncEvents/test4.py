from SyncEvents import *

mutex = CreateMutex('Local\\test')
WaitForMutex(mutex)
print('test')
ReleaseMutex(mutex)
CloseMutex(mutex)
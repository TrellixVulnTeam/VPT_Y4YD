from SyncEvents import *

mutex = CreateMutex('Local\\test')
WaitForMutex(mutex)
input('')
ReleaseMutex(mutex)
CloseMutex(mutex)

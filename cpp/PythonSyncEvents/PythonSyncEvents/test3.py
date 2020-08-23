from syncevents import *

mutex = CreateMutex('LOCAL\test')
WaitForMutex(mutex)
input('')
ReleaseMutex(mutex)
CloseMutex(mutex)

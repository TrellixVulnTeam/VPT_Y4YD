from SyncEvents import *

event = OpenEvent('Local\\test')
WaitForEvent(event)
print('test')
CloseEvent(event)
from syncevents import *

event = OpenEvent('LOCAL\test')
WaitForEvent(event)
print('test')
CloseEvent(event)
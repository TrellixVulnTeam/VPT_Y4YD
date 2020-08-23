from SharedMemory import *

handle = CreateSharedMemory("Local\\test", 1024)
view = WriteSharedMemory(handle, "this is a test")

filename = 'test2.py'
with open(filename, "rb") as source_file:
    code = compile(source_file.read(), filename, "exec")
exec(code)

UnmapSharedView(view)
CloseSharedMemory(handle)
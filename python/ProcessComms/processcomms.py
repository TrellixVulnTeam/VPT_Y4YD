from SharedMemory import *
from SyncEvents import *
import threading
import random
import string

commDict = {}
commTermDict = {}

#Credit: https://pynative.com/python-generate-random-string/
def get_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def RunIfDefined(arg, func):
    if arg is not None:
        func(arg)
        pass
    pass

def Catch(arg, func):
    try:
        func(arg)
        pass
    except:
        pass
    pass

def Catch2(arg1, arg2, func):
    try:
        func(arg1, arg2)
        pass
    except:
        pass
    pass

def RunIfDefinedAndCatch(arg, func):
    Catch2(arg, func, RunIfDefined)

def CreateOrOpenSharedMemory(name, size, shouldCreate):
    if shouldCreate:
        return CreateSharedMemory(name, size)
    return OpenSharedMemory(name)

def CreateOrOpenEvent(name, shouldCreate, manualReset=True, initialState=False):
    if shouldCreate:
        return CreateEvent(name, manualReset, initialState)
    return OpenEvent(name)

def CreateOrOpenMutex(name, shouldCreate, isOwned=False):
    if shouldCreate:
        return CreateMutex(name, isOwned)
    return OpenMutex(name)

def CreateOrOpenComm(name, size, isServer):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    serverMemoryHandle = None
    clientMemoryHandle = None
    serverSendEvent = None
    clientSendEvent = None
    serverRecieveEvent = None
    clientRecieveEvent = None
    serverMemoryLock = None
    clientMemoryLock = None
    terminateEvent = None

    try:
        serverMemoryHandle = CreateOrOpenSharedMemory('Local\\' + name + '_servermemory', size, isServer)
        clientMemoryHandle = CreateOrOpenSharedMemory('Local\\' + name + '_clientmemory', size, isServer)
        serverSendEvent = CreateOrOpenEvent('Local\\' + name + '_serversendevent', isServer, manualReset=False)
        clientSendEvent = CreateOrOpenEvent('Local\\' + name + '_clientsendevent', isServer, manualReset=False)
        serverRecieveEvent = CreateOrOpenEvent('Local\\' + name + '_serverrecieveevent', isServer, manualReset=False)
        clientRecieveEvent = CreateOrOpenEvent('Local\\' + name + '_clientrecieveevent', isServer, manualReset=False)
        serverMemoryLock = CreateOrOpenMutex('Local\\' + name + '_servermemorylock', isServer)
        clientMemoryLock = CreateOrOpenMutex('Local\\' + name + '_clientmemorylock', isServer)
        terminateEvent = CreateEvent('Local\\' + name + '_' + ('client', 'server')[isServer] + 'terminationevent')
        pass
    except (SharedMemoryError, SyncError):
        RunIfDefinedAndCatch(serverMemoryHandle, CloseSharedMemory)
        RunIfDefinedAndCatch(clientMemoryHandle, CloseSharedMemory)
        RunIfDefinedAndCatch(serverSendEvent, CloseEvent)
        RunIfDefinedAndCatch(clientSendEvent, CloseEvent)
        RunIfDefinedAndCatch(serverRecieveEvent, CloseEvent)
        RunIfDefinedAndCatch(clientRecieveEvent, CloseEvent)
        RunIfDefinedAndCatch(serverMemoryLock, CloseMutex)
        RunIfDefinedAndCatch(clientMemoryLock, CloseMutex)
        RunIfDefinedAndCatch(terminateEvent, CloseEvent)
        return False
    key = None
    while True:
        key = get_random_string(32)
        if key not in commDict:
            break
        pass
    commDict[key]=(serverMemoryHandle, clientMemoryHandle, serverSendEvent, clientSendEvent, serverRecieveEvent, clientRecieveEvent, serverMemoryLock, clientMemoryLock, terminateEvent)
    commTermDict[key]=False
    return True

def doReadComm(comm, func, isServer):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    commT = commDict[comm]
    memory = commT[(0,1)[isServer]]
    sendEvent = commT[(3,2)[isServer]]
    recieveEvent = commT[(4,5)[isServer]]
    lock = commT[(7,6)[isServer]]
    terminateEvent = commT[8]
    while True:
        try:
            WaitForEvent(sendEvent)
            if(commTermDict[comm]):
                break
            WaitForMutex(lock)
            msg = None
            try:
                msg = ReadSharedMemory(memory)
                pass
            finally:
                Catch(lock, ReleaseMutex)
                Catch(recieveEvent, SetEvent)
                pass
            func(msg)
            pass
        except:
            pass
        pass
    Catch(terminateEvent, SetEvent)
    pass

def StartReadComm(comm, func, isServer, recieveInNewThread=False):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    funcP = (func, None)[recieveInNewThread]
    if(recieveInNewThread):
        def threadWrappedFunc(msg):
            t = threading.Thread(target=func, args=(msg,))
            t.start()
            pass
        funcP = threadWrappedFunc
        pass
    t = threading.Thread(target=doReadComm, args=(comm, funcP, isServer,))
    t.start()
    pass


def WriteToComm(comm, msg, isServer):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    commT = commDict[comm]
    memory = commT[(1,0)[isServer]]
    sendEvent = commT[(2,3)[isServer]]
    recieveEvent = commT[(5,4)[isServer]]
    lock = commT[(6,7)[isServer]]
    WaitForMutex(lock)
    try:
        view = WriteWriteSharedMemory(memory, msg)
        pass
    finally:
        Catch(lock, ReleaseMutex)
        pass
    try:
        SetEvent(sendEvent)
        WaitForEvent(recieveEvent)
        pass
    finally:
        Catch(view, UnmapSharedView)
        pass
    pass

def CloseComm(comm, isServer):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    commT = commDict[comm]
    serverMemoryHandle = commT[0]
    clientMemoryHandle = commT[1]
    serverSendEvent = commT[2]
    clientSendEvent = commT[3]
    serverRecieveEvent = commT[4]
    clientRecieveEvent = commT[5]
    serverMemoryLock = commT[6]
    clientMemoryLock = commT[7]
    terminateEvent = commT[8]
    commTermDict[comm] = True
    Catch(terminateEvent, WaitForEvent)
    Catch(serverMemoryHandle, CloseSharedMemory)
    Catch(clientMemoryHandle, CloseSharedMemory)
    Catch(serverSendEvent, CloseEvent)
    Catch(clientSendEvent, CloseEvent)
    Catch(serverRecieveEvent, CloseEvent)
    Catch(clientRecieveEvent, CloseEvent)
    Catch(serverMemoryLock, CloseMutex)
    Catch(clientMemoryLock, CloseMutex)
    Catch(terminateEvent, CloseEvent)
    commDict.pop(comm)
    commTermDict.pop(comm)
    pass
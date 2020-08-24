from SharedMemory import *
from SyncEvents import *
import threading
import random
import string
import asyncio

commDict = {}
commTermDict = {}

#Credit: https://pynative.com/python-generate-random-string/
def get_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def RunIfDefined(func, *args):
    if arg is not None:
        func(*args)
        pass
    pass

def Catch(func, *args):
    try:
        func(*args)
        pass
    except:
        pass
    pass

def RunIfDefinedAndCatch(func, *args):
    Catch(RunIfDefined, func, *args)
    pass

async def AsAsync(func, *args):
    await asyncio.get_event_loop().run_in_executor(None, func, *args)
    pass

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
        RunIfDefinedAndCatch(CloseSharedMemory, serverMemoryHandle)
        RunIfDefinedAndCatch(CloseSharedMemory, clientMemoryHandle)
        RunIfDefinedAndCatch(CloseEvent, serverSendEvent)
        RunIfDefinedAndCatch(CloseEvent, clientSendEvent)
        RunIfDefinedAndCatch(CloseEvent, serverRecieveEvent)
        RunIfDefinedAndCatch(CloseEvent, clientRecieveEvent)
        RunIfDefinedAndCatch(CloseMutex, serverMemoryLock)
        RunIfDefinedAndCatch(CloseMutex, clientMemoryLock)
        RunIfDefinedAndCatch(CloseEvent, terminateEvent)
        return None
    key = None
    while True:
        key = get_random_string(32)
        if key not in commDict:
            break
        pass
    commDict[key]=(serverMemoryHandle, clientMemoryHandle, serverSendEvent, clientSendEvent, serverRecieveEvent, clientRecieveEvent, serverMemoryLock, clientMemoryLock, terminateEvent)
    commTermDict[key]=False
    return key

async def doReadComm(comm, func, isServer):
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
            await AsAsync(WaitForEvent, sendEvent)
            if(commTermDict[comm]):
                break
            await AsAsync(WaitForMutex, lock)
            msg = None
            try:
                msg = ReadSharedMemory(memory)
                pass
            finally:
                Catch(ReleaseMutex, lock)
                Catch(SetEvent, recieveEvent)
                pass
            func(msg)
            pass
        except:
            pass
        pass
    Catch(SetEvent, terminateEvent)
    pass

def doDoReadComm(comm, func, isServer):
    asyncio.new_event_loop().run_until_complete(doReadComm(comm, func, isServer))

def StartReadComm(comm, func, isServer, recieveInNewThread=False):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    funcP = (func, None)[recieveInNewThread]
    if(recieveInNewThread):
        def threadWrappedFunc(msg):
            t = threading.Thread(target=func, args=(msg))
            t.start()
            pass
        funcP = threadWrappedFunc
        pass
    t = threading.Thread(target=doDoReadComm, args=(comm, funcP, isServer))
    t.start()
    pass


async def WriteToCommAsync(comm, msg, isServer):
    if comm not in commDict:
        raise ValueError('Comm Does Not Exist')
    commT = commDict[comm]
    memory = commT[(1,0)[isServer]]
    sendEvent = commT[(2,3)[isServer]]
    recieveEvent = commT[(5,4)[isServer]]
    lock = commT[(6,7)[isServer]]
    await AsAsync(WaitForMutex, lock)
    try:
        view = WriteSharedMemory(memory, msg)
        pass
    finally:
        Catch(ReleaseMutex, lock)
        pass
    try:
        SetEvent(sendEvent)
        await AsAsync(WaitForEvent, recieveEvent)
        pass
    finally:
        Catch(UnmapSharedView, view)
        pass
    pass


def WriteToComm(comm, msg, isServer):
    asyncio.new_event_loop().run_until_complete(WriteToCommAsync(comm, msg, isServer))
    pass

async def CloseCommAsync(comm):
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
    await AsAsync(Catch, terminateEvent, WaitForEvent)
    Catch(CloseSharedMemory, serverMemoryHandle)
    Catch(CloseSharedMemory, clientMemoryHandle)
    Catch(CloseEvent, serverSendEvent)
    Catch(CloseEvent, clientSendEvent)
    Catch(CloseEvent, serverRecieveEvent)
    Catch(CloseEvent, clientRecieveEvent)
    Catch(CloseMutex, serverMemoryLock)
    Catch(CloseMutex, clientMemoryLock)
    Catch(CloseEvent, terminateEvent)
    commDict.pop(comm)
    commTermDict.pop(comm)
    pass

def CloseComm(comm):
    asyncio.new_event_loop().run_until_complete(CloseCommAsync(comm))
    pass
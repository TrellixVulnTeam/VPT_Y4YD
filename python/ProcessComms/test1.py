import processcomms

comm = processcomms.CreateOrOpenComm('testcomm', 1024, True)
processcomms.StartReadComm(comm, print, True)
input('')
processcomms.WriteToComm(comm, 'testfrom1', True)
input('')
processcomms.CloseComm(comm)
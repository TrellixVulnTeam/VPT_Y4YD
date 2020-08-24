import processcomms

comm = processcomms.CreateOrOpenComm('testcomm', 1024, False)
processcomms.StartReadComm(comm, print, False)
input('')
processcomms.WriteToComm(comm, 'testfrom2', False)
input('')
processcomms.CloseComm(comm)
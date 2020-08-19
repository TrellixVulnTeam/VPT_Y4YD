#include "SharedMemory.h"
#include <map>
#include <comdef.h>
#include <tchar.h>

map<string, HANDLE> fileHandles;
map<string, LPTSTR> viewHandles;

static TCHAR* ANSItoUnicode(const char* text) {
    wchar_t* out = new wchar_t[strlen(text) + 1];
    size_t ncc = 0;
    mbstowcs_s(&ncc, out, strlen(text) + 1, text, _TRUNCATE);
    return reinterpret_cast<TCHAR*>(out);
}

static char* UnicodetoANSI(wchar_t* text) {
    char* out = new char[2 * (wcslen(text) + 1)];
    size_t ncc = 0;
    wcstombs_s(&ncc, out, 2 * (wcslen(text) + 1), text, _TRUNCATE);
    return out;
}

static CHAR* ANSItoANSI(const char* text) {
    return const_cast<CHAR*>(text);
}

#ifdef UNICODE
#define CP(text) ANSItoUnicode(text)
#else
#define CP(text) ANSItoANSI(text)
#endif

#ifdef UNICODE
#define CPI(text) UnicodetoANSI(text)
#else
#define CPI(text) ANSItoANSI(text)
#endif

//Credit: https://stackoverflow.com/questions/440133/how-do-i-create-a-random-alpha-numeric-string-in-c
static void gen_random(char* s, const int len) {
    static const char alphanum[] =
        "0123456789"
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "abcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < len; ++i) {
        s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
    }

    s[len] = 0;
}

//Credit: https://stackoverflow.com/questions/2709713/how-to-convert-unsigned-long-to-string
static string sul(unsigned long num) {
    return to_string(num);
}

static const char* GetLastErrorExport() {
    string out = "Error Occured: " + sul(GetLastError());
    return out.c_str();
}

static string randomString() {
    char* randChars = new char[32];
    gen_random(randChars, 32);
    return string(randChars);
}

static string SharedMemory::Create(const char* name, size_t size) throw(SharedMemoryException) {
    HANDLE memoryHandle = CreateFileMappingA(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, size, name);
    if (memoryHandle == NULL) {
        throw new SharedMemoryException(GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (fileHandles.count(key) != 0);
    fileHandles.insert(make_pair(key, memoryHandle));
    return key;
}

static string SharedMemory::Open(const char* name) throw(SharedMemoryException) {
    HANDLE memoryHandle = OpenFileMappingA(FILE_MAP_ALL_ACCESS, FALSE, name);
    if (memoryHandle == NULL) {
        throw new SharedMemoryException(GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (fileHandles.count(key) != 0);
    fileHandles.insert(make_pair(key, memoryHandle));
    return key;
}

static string SharedMemory::Read(string handle) throw(SharedMemoryException) {
    if (fileHandles.count(handle) != 1) {
        throw new SharedMemoryException("Handle Does Not Exist");
        return NULL;
    }
    LPTSTR data = (LPTSTR)MapViewOfFile(fileHandles.find(handle)->second, FILE_MAP_ALL_ACCESS, 0, 0, 0);
    if (data == NULL) {
        throw new SharedMemoryException(GetLastErrorExport());
        return NULL;
    }
    string out(CPI(data));
    UnmapViewOfFile(data);
    return out;
}

static string SharedMemory::Write(string handle, const char* text) throw(SharedMemoryException) {
    if (fileHandles.count(handle) != 1) {
        throw new SharedMemoryException("Handle Does Not Exist");
        return NULL;
    }
    LPTSTR data = (LPTSTR)MapViewOfFile(fileHandles.find(handle)->second, FILE_MAP_ALL_ACCESS, 0, 0, 0);
    if (data == NULL) {
        throw new SharedMemoryException(GetLastErrorExport());
        return NULL;
    }
    TCHAR* msg = CP(text);
    CopyMemory((PVOID)data, msg, (_tcslen(msg) * sizeof(TCHAR)));
    string key;
    do {
        key = randomString();
    } while (viewHandles.count(key) != 0);
    viewHandles.insert(make_pair(key, data));
    return key;
}

static void SharedMemory::UnmapView(string view) throw(SharedMemoryException) {
    if (viewHandles.count(view) != 1) {
        throw new SharedMemoryException(GetLastErrorExport());
        return;
    }
    viewHandles.erase(view);
    UnmapViewOfFile(viewHandles.find(view)->second);
}

static void SharedMemory::Close(string handle) throw(SharedMemoryException) {
    if (fileHandles.count(handle) != 1) {
        throw new SharedMemoryException("Handle Does Not Exist");
        return;
    }
    CloseHandle(fileHandles.find(handle)->second);
    fileHandles.erase(handle);
}
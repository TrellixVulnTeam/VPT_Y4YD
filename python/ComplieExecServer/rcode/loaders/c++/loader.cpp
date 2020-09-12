#include <iostream>
#include <vector>
#include <map>
#include <fstream>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <algorithm>
using namespace std;

typedef void(*runfunc)(vector <string>, vector<vector <string>>, vector<int>, vector<vector <int>>);

vector <int> VecStrTInt(vector<string> vectc){
	vector <int> convertedval;
	for(unsigned int i = 0; i < vectc.size(); i++){
		convertedval.push_back(stoi(vectc[i]));
	}
	return convertedval;

}

int main(int argc, char* argv[]){
	vector <string> args;

	try{

		//temp storage init
		vector <string> conststring;
		vector <vector <string>> dstrings;

		vector <int> constint;
		vector <vector <int>> dints;
		//temp storage init

		if(argc > 1){
			args.assign(argv + 1, argv + argc);
		}

		else{
			throw "error";
		}
		string tpath = args[0] + "_tests.tcfg";
		ifstream tconfig(tpath.c_str());
		vector <string> tclbl;
		string tt;

		while(getline(tconfig, tt)){
			tclbl.push_back(tt);
		}

		tconfig.close();
		string format = tclbl[0];

		tclbl.erase(tclbl.begin());
		unsigned int sc = stoi(tclbl[0]);
		tclbl.erase(tclbl.begin());
		tclbl.erase(remove(tclbl.begin(), tclbl.end(), ""), tclbl.end());
		//static data
		for(int i = 0; i < sc; i++){
			switch(format[i]){
				case 's' :
					conststring.push_back(tclbl[i]);
					break;

				case 'i' :
					constint.push_back(stoi(tclbl[i]));
					break;

				default :
					throw "error";
			}
		}

		//dynamic data
		vector <vector <string>> dd;
		dd.push_back(vector <string>());
		for (unsigned int i = sc; i < tclbl.size(); i++){
			if(tclbl[i][tclbl[i].length() - 1] == ';'){
				dd[dd.size() - 1].push_back(tclbl[i].substr(0, tclbl[i].length() - 1));
				dd.push_back(vector<string>());
			}
			else{
				dd[dd.size() - 1].push_back(tclbl[i]);
			}
		}

		for(unsigned int i = 0; i < dd.size() - 1; i++){
			switch (format[sc + i]){
				case 's' :
					dstrings.push_back(dd[i]);
					break;

				case 'i' :
					dints.push_back(VecStrTInt(dd[i]));
					break;

				default :
					throw "error";
			}
		}
		cout << dints[0][0] << endl;
		cout << dstrings[0][0] << endl;
		cout << args[1].c_str() << endl;
		//init libarays
		void *handle;
		char *error;
		runfunc runfuncADDR;
		string path = "./" + args[1];
		handle = dlopen(path.c_str(), RTLD_LAZY);
		if(!handle){
			throw "error";
		}
		dlerror();
		runfuncADDR = (runfunc)dlsym(handle, "RUN");
		if ((error = dlerror()) != NULL){
			throw "error";
		}
		(*runfuncADDR)(conststring, dstrings, constint, dints);
		dlclose(handle);

	}
	catch(const char* err){
		cout << err << endl;
		exit(1);
	}


}

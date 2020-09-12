#include <iostream>
#include <vector>
#include <string>
using namespace std;

extern "C" void RUN(vector <string> conststring, vector<vector <string>> dstrings, vector<int> constint, vector<vector <int>>);

void RUN(vector <string> conststring, vector<vector <string>> dstrings, vector<int> constint, vector<vector <int>>){
	cout << "test" << endl;
}

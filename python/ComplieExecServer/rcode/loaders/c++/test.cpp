#include <iostream>
#include <vector>
#include <string>
using namespace std;

extern "C" void RUN(vector <string> conststring, vector<vector <string>> dstrings, vector<int> constint, vector<vector <int>> dints);

void RUN(vector <string> conststring, vector<vector <string>> dstrings, vector<int> constint, vector<vector <int>> dints){
	cout << "test" << endl;
}

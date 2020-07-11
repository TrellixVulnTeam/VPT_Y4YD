#pragma once
#include "../../Include.h"
#include "projects/VPT/Packet.h"
#include "projects/VPT/client.h"
#include <map>
#include "projects/VPT/Utils.h"
#include "projects/VPT/ResultId.h"
#include "CppPython.h"

class Scene {
public:
	Scene();
	virtual void Init(AppInstance* instance);
	virtual void PostInit() {}
	virtual void LoadStaticComponents() = 0;
	virtual void LoadComponents() = 0;
	virtual void AttachListeners();
	virtual void Update();
	virtual void Input(SDL_Event e);
	virtual void Draw();
	virtual void ProcessPacket(Packet p) {}
	virtual Scene& Create() = 0;
	const int id;
	AppInstance* instance;
	ComponentManager cm;
	vector <AppObject*> Objects;
	vector <AppObject*> Overlays;
	bool needsCollisions = true;
	bool doneInitOnInit = true;
};

class StaticScene : public Scene {
public:
	virtual Scene& StaticCreate(Scene& classRef);
	virtual Scene& DoCreate() = 0;
	static map<string, Scene*>* staticScenes;
};

class DynamicScene : public Scene {
public:
	DynamicScene() { doneInitOnInit = false; }
	virtual void PostInit();
	virtual void PostInitD() {};
	virtual void BeginLoadingDynamicComponents() = 0;
};

class LoadingScreen : public StaticScene {
public:
	LoadingScreen() { needsCollisions = false; }
	virtual void LoadStaticComponents();
	virtual void LoadComponents() {}
	virtual void Update();
	virtual Scene& Create() { return StaticCreate(*this); }
	virtual Scene& DoCreate() { return *new LoadingScreen(); }
	double textSpeed;
	static Text* text;
	static LoadingSymbol* loadingSymbol;
};

class LoginScreen : public Scene {
public:
	LoginScreen() { userIcon = nullptr; }
	virtual void LoadStaticComponents();
	virtual void LoadComponents();
	virtual void Draw();
	void login();
	void doLogin();
	void getProfilePic();
	virtual Scene& Create() { return *new LoginScreen(); }
	TextField* usernameField;
	TextField* passwordField;
	SDL_Texture* userIcon;
	static Text* text;
	static SimpleButton* loginButton;
};

class StandardQuestion : public Scene {
public:
	StandardQuestion(string question) { question_m = question; }
	virtual void LoadStaticComponents();
	virtual void LoadComponents();
	string GetInput;
	virtual Scene& Create() { return *new StandardQuestion(question_m); }
	void Input(SDL_Event e);
	TextField* answer_box;
	Text* prompt;
	string question_m;
	SimpleButton* Monoca_Editor_open;
	SimpleButton* Esc_Menu_open;
	void onClickM(SimpleButton* sb);
	void onClickEM(SimpleButton* sb);
};

class escMenu : public Scene {
public:
	virtual void LoadStaticComponents();
	virtual void LoadComponents();
	virtual Scene& Create() { return *new escMenu(); }
	//these will be replaced with better buttons later
	Text* Title;
	SimpleButton* ResumeTest;
	SimpleButton* options;
	SimpleButton* Exit;
	
};


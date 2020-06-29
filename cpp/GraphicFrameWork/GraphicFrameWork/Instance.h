#pragma once
#include <iostream>
#include <functional>
#include <mutex>
#include "SDL.h"
#include "SDL_image.h"
#include "AppObject.h"
#include "Component.h"
#include "projects/VPT/Flags.h"
#include "projects/VPT/Utils.h"
using namespace std;
class Scene;
class AppInstance
{
public:
	AppInstance();
	void BasicInit(const char* window_title, int w, int h);
	virtual void Init(const char* window_title, int w, int h) { BasicInit(window_title, w, h); }
	void BasicUpdate();
	virtual void Update() { BasicUpdate(); }
	void BasicInput();
	virtual void Input() { BasicInput(); };
	void BasicDraw();
	virtual void Draw() { BasicDraw(); };
	void BasicLoop();
	virtual void Loop() { BasicLoop(); }
	virtual void Cleanup() {}
	void reportError();
	void addOverlay(AppObject* overlay);
	void BeginLoadingScene(Scene& scene);
	void FinishSceneLoading(Scene& scene);
	Scene& GetActiveScene();
	void RequestSDLFunct(function<void()> funct);
	virtual void RunRequestedSDLFuncts();

	//
	SDL_Window* Win;
	SDL_Renderer* renderer;
	SDL_Event e;
	bool running = true;
	ComponentManager cm;
	vector <AppObject*> AppObjects;
	vector <AppObject*> Overlays;
	const int FPS = 60;
	const int FrameDelay = 1000 / FPS;
	Uint32 framestart;
	int frametime = INT_MAX;
	int cnt;
	int numErrors = 0;
	Uint32 lastError = 0;
	Scene* currentScene;
	bool isDisplayingScene;
	bool isDynamicScene;
	SDL_Color backgroundColor;
	Utils::SDL_Dimension windowSize;
	vector<function<void()>> requestedSDLFuncts;
	//

};

#include "projects/VPT/Scene.h"
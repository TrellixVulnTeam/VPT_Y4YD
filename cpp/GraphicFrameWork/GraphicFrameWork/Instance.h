#pragma once
#include <iostream>
#include "SDL.h"
#include "SDL_image.h"
#include "AppObject.h"
#include "Component.h"
using namespace std;
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

	//
	SDL_Window* Win;
	SDL_Renderer* renderer;
	SDL_Event e;
	bool running = true;
	ComponentManager cm;
	vector <AppObject*> AppObjects;
	const int FPS = 60;
	const int FrameDelay = 1000 / FPS;
	Uint32 framestart;
	int frametime = INT_MAX;
	int cnt;
	//

};


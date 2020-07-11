#include "TestV.h"

TestV::TestV::TestV()
{
}

void TestV::TestV::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	BeginLoadingScene(escMenu().Create());
}

void TestV::TestV::Draw()
{
	SDL_RenderClear(renderer);
	GetActiveScene().Draw();
	SDL_RenderPresent(renderer);
}

void TestV::TestV::Update()
{
	GetActiveScene().Update();
}

void TestV::TestV::Input(bool wasEvent, SDL_Event e)
{
	if (wasEvent && e.window.windowID == windowId) {
		if (e.type == SDL_QUIT || (e.type == SDL_WINDOWEVENT && e.window.event == SDL_WINDOWEVENT_CLOSE)) { running = false; }
		GetActiveScene().Input(e);
	}
}

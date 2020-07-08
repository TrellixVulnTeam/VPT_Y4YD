#include "TestV.h"

TestV::TestV::TestV()
{
}

void TestV::TestV::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	BeginLoadingScene(StandardQuestion("SHIT").Create());
	GetActiveScene().Init(this);
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

void TestV::TestV::Input()
{
	if (SDL_PollEvent(&e)) {
		if (e.type == SDL_QUIT) { running = false; }
		GetActiveScene().Input(e);
	}
}

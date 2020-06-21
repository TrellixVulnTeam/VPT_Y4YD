#include "Instance.h"


AppInstance::AppInstance()
{

}

void AppInstance::BasicInit(const char* window_title, int w, int h)
{
	Win = SDL_CreateWindow(window_title, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, w, h, 0);
	renderer = SDL_CreateRenderer(Win, -1, SDL_RENDERER_ACCELERATED);
	SDL_SetRenderDrawBlendMode(renderer, SDL_BLENDMODE_BLEND);
	SDL_ShowCursor(1);
	SDL_SetRenderDrawColor(renderer, 255, 255, 255, 255);
}

void AppInstance::BasicUpdate()
{
	for (unsigned int i = 0; i < cm.UpdateSectorComponents.size(); i++) {
		cm.UpdateSectorComponents[i]->run(AppObjects);
	}
}

void AppInstance::BasicInput()
{
	for (unsigned int i = 0; i < cm.InputSectorComponents.size(); i++) {
		cm.InputSectorComponents[i]->run(AppObjects);
	}
	if (SDL_PollEvent(&e)) {
		if (e.type == SDL_QUIT) { running = false; }
	}
}

void AppInstance::BasicDraw()
{
	for (unsigned int i = 0; cm.DrawSectorComponents.size(); i++) {
		cm.DrawSectorComponents[i]->run(AppObjects);
	}
}

void AppInstance::BasicLoop()
{
	while (running) {
		while (running) {
			Update();
			Input();
			if (frametime > FrameDelay) {
				framestart = SDL_GetTicks();
				Draw();
			}
			frametime = SDL_GetTicks() - framestart;
			if (cnt == INT_MAX) {
				cnt = 1;
			}
			cnt++;
		}
		Cleanup();
	}
	Cleanup();
}

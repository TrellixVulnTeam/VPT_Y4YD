#include "Instance.h"

mutex requestLock;

AppInstance::AppInstance()
{
	backgroundColor = SDL_Color{ 225, 225, 225, 255 };
}

void AppInstance::BasicInit(const char* window_title, int w, int h)
{
	Win = SDL_CreateWindow(window_title, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, w, h, 0);
	renderer = SDL_CreateRenderer(Win, -1, SDL_RENDERER_ACCELERATED);
	SDL_SetRenderDrawBlendMode(renderer, SDL_BLENDMODE_BLEND);
	SDL_ShowCursor(1);
	Utils::SDL_SetRenderDrawSDLColor(renderer, backgroundColor);
	isDisplayingScene = false;
	isDynamicScene = false;
	windowSize = Utils::SDL_Dimension();
	SDL_GetWindowSize(Win, &windowSize.width, &windowSize.height);
	currentScene = &LoadingScreen().Create();
	currentScene->Init(this);
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
#ifndef USE_DEBUGGER
			try {
#endif
				RunRequestedSDLFuncts();
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
#ifndef USE_DEBUGGER
			}
			catch (const runtime_error & re) {
				cout << "Runtime Error Occured: " << re.what() << endl;
				reportError();
			}
			catch (const exception & ex) {
				cout << "Exception Occured: " << ex.what() << endl;
				reportError();
			}
			catch (...) {
				cout << "Unknown Error Occured" << endl;
				reportError();
			}
#endif
		}
		Cleanup();
	}
	Cleanup();
}

void AppInstance::reportError() {
	if (SDL_GetTicks() - lastError > 50) {
		numErrors = 0;
	}
	lastError = SDL_GetTicks();
	numErrors++;
	if (numErrors > 10) {
		cout << "Multiple Errors Have Occured Within a Short Space of Time. Terminating..." << endl;
		exit(EXIT_FAILURE);
	}
}

void AppInstance::addOverlay(AppObject* overlay) {
	int id = -1;
	for (int i = 0; i < Overlays.size(); i++) {
		if (Overlays[i] == nullptr) {
			id = i;
			break;
		}
	}
	if (id != -1) {
		overlay->id = id;
		Overlays[id] = overlay;
		return;
	}
	overlay->id = Overlays.size();
	Overlays.push_back(overlay);
}

void AppInstance::BeginLoadingScene(Scene& scene) {
	isDisplayingScene = false;
	isDynamicScene = dynamic_cast<DynamicScene*>(&scene) == nullptr;
	currentScene = &scene;
	currentScene->Init(this);
}

void AppInstance::FinishSceneLoading(Scene& scene) {
	if (&scene == currentScene) {
		isDisplayingScene = true;
	}
}

Scene& AppInstance::GetActiveScene() {
	return isDisplayingScene ? *currentScene : LoadingScreen().Create();
}

void AppInstance::RequestSDLFunct(function<void()> funct) {
	lock_guard<mutex> rflg(requestLock);
	requestedSDLFuncts.push_back(funct);
}

void AppInstance::RunRequestedSDLFuncts() {
	lock_guard<mutex> rflg(requestLock);
	for (function<void()> funct : requestedSDLFuncts) {
		funct();
	}
	requestedSDLFuncts.clear();
}
#include "Instance.h"

//Note: Don't exceed 16 windows
vector<AppInstance*> AppInstance::multiInstances(16);
mutex requestLock;
int numErrors = 0;
Uint32 lastError = 0;

AppInstance::AppInstance()
{
	backgroundColor = SDL_Color{ 225, 225, 225, 255 };
}

void AppInstance::BasicInit(const char* window_title, int w, int h)
{
	Win = SDL_CreateWindow(window_title, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, w, h, 0);
	windowId = SDL_GetWindowID(Win);
	string iconPath = dir + "WindowIcon.png";
	SDL_SetWindowIcon(Win, IMG_Load(iconPath.c_str()));
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
	AppObjects.push_back(new AppObject());
	AppObjects[0]->PreInit("");
	AppObjects[0]->Init(renderer, 1, 1, 0, 0);
}

void AppInstance::BasicUpdate()
{
	for (unsigned int i = 0; i < cm.UpdateSectorComponents.size(); i++) {
		int UpdateVal = cm.UpdateSectorComponents[i]->run(AppObjects);
		AppObject* object1 = AppObjects[cm.UpdateSectorComponents[i]->parent_m->id];
		if (TextField* TextFieldObj = dynamic_cast<TextField*>(object1)) {
			SDL_StartTextInput();
		}
		else {
			SDL_StopTextInput();
		}
		object1->collide(UpdateVal);
	}
	for (AppObject* object : AppObjects) {
		object->update();
	}
}

void AppInstance::BasicInput(bool wasEvent, SDL_Event e)
{
	for (unsigned int i = 0; i < cm.InputSectorComponents.size(); i++) {
		cm.InputSectorComponents[i]->run(AppObjects);
	}
	if (wasEvent) {
		if (e.window.windowID == windowId) {
			if (e.type == SDL_QUIT || (e.type == SDL_WINDOWEVENT && e.window.event == SDL_WINDOWEVENT_CLOSE)) { running = false; }
			if (e.type == SDL_MOUSEMOTION) {
				AppObjects[0]->x_m = e.motion.x;
				AppObjects[0]->y_m = e.motion.y;
			}
			for (AppObject* object : AppObjects) {
				object->input(e);
			}
			for (AppObject* overlay : Overlays) {
				if (overlay != nullptr) {
					overlay->input(e);
				}
			}
		}
	}
}

void AppInstance::BasicDraw()
{
	SDL_RenderClear(renderer);
	for (unsigned int i = 0; cm.DrawSectorComponents.size(); i++) {
		cm.DrawSectorComponents[i]->run(AppObjects);
	}
	for (AppObject* object : AppObjects) {
		object->draw();
	}
	SDL_RenderPresent(renderer);
}

void AppInstance::BasicLoop()
{
	while (running) {
#ifndef USE_DEBUGGER
		try {
#endif
			SDL_Event e;
			bool wasEvent = SDL_PollEvent(&e);
			RunRequestedSDLFuncts();
			Update();
			Input(wasEvent, e);
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
	overlay->id = (int)Overlays.size();
	Overlays.push_back(overlay);
}

void AppInstance::BeginLoadingScene(Scene& scene) {
	if (isDisplayingScene) {
		prevScene = currentScene;
	}
	isDisplayingScene = false;
	isDynamicScene = dynamic_cast<DynamicScene*>(&scene) == nullptr;
	currentScene = &scene;
	currentScene->Init(this);
}

void AppInstance::RevertSceneLoading(Scene& currentScene) {
	if (&currentScene == this->currentScene) {
		this->currentScene = prevScene;
		isDisplayingScene = true;
	}
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

void AppInstance::RunMultiLoop(vector<AppInstance*> instances) {
	multiInstances = instances;
	bool shouldRun = true;
	while (shouldRun) {
		shouldRun = false;
#ifndef USE_DEBUGGER
		try {
#endif
			SDL_Event e;
			bool wasEvent = SDL_PollEvent(&e);
			for (AppInstance* instance : multiInstances) {
				if (!instance->running) {
					continue;
				}
				shouldRun = true;
				instance->RunRequestedSDLFuncts();
				instance->Update();
				instance->Input(wasEvent, e);
				if (instance->frametime > instance->FrameDelay) {
					instance->framestart = SDL_GetTicks();
					instance->Draw();
				}
				instance->frametime = SDL_GetTicks() - instance->framestart;
				if (instance->cnt == INT_MAX) {
					instance->cnt = 1;
				}
				instance->cnt++;
			}
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
	for (AppInstance* instance : multiInstances) {
		instance->Cleanup();
	}
}
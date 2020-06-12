#include "editor.h"
const string dir = "..\\";
void editor::editor::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	AppObjects.push_back(new AppObject());
	AppObjects[0]->PreInit("");
	AppObjects[0]->Init(renderer, 1, 1, 0, 0);
	
	TextBoxData tbd;
	tb = new TextBox("C:\\Users\\richa\\source\\repos\\VPT\\cpp\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\PrinceValiant.ttf", 
	tbd.textsize, tbd.x_offset, tbd.y_offset, "Button");
	tb->Init(renderer, tbd.w, tbd.h, 0, 0);
	AppObjects.push_back(tb);

	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		AppObjects[i]->id = i;
	}

	cm.AttachComponent(new CollisionBox(AppObjects), AppObjects[AppObjects.size() - 1]);
}

void editor::editor::Draw()
{
	SDL_RenderClear(renderer);
	for (AppObject* object : AppObjects) {
		object->draw();
	}
	SDL_RenderPresent(renderer);
}

void editor::editor::Update()
{
	for (Component* c : cm.UpdateSectorComponents) {
		UpdateVal = c->run(AppObjects);
		AppObject* object = AppObjects[c->parent_m->id];
		if (TextBox* obj = dynamic_cast<TextBox*>(object)) {
			if (UpdateVal != -1) {
				if (obj->message == "Button") {
					ButtontbClicked = true;
				}
			}
			else {
				ButtontbClicked = false;
			}

		}
		else {
			ButtontbClicked = false;
		}
	}
	for (AppObject* object : AppObjects) {
		object->update();
	}
}

void editor::editor::Input()
{
	for (Component* c : cm.InputSectorComponents) {
		c->run(AppObjects);
	}
	if (SDL_PollEvent(&e)) {
		if (e.type == SDL_QUIT) { running = false; }
		if (e.type == SDL_MOUSEMOTION) {
			AppObjects[0]->x_m = e.motion.x;
			AppObjects[0]->y_m = e.motion.y;
			for (AppObject* object : AppObjects) {
				if (PlaceableBounding* obj = dynamic_cast<PlaceableBounding*>(object)) {
					if (object->id == 1) {
						object->x_m = AppObjects[0]->x_m;
						object->y_m = AppObjects[0]->y_m;
					}
				}
			}
		}
		if (e.type == SDL_MOUSEBUTTONUP) {
			if (ButtontbClicked == true){
				AppObjects.push_back(new PlaceableButton());
				AppObjects[AppObjects.size() - 1]->PreInit("C:\\Users\\richa\\source\\repos\\VPT\\cpp\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\bounding.png");
				AppObjects[AppObjects.size() - 1]->Init(renderer, 100, 100,400, 400);
				AppObjects[AppObjects.size() - 1]->id = 1;
				AppObjects[AppObjects.size() - 1]->x_m = AppObjects[0]->x_m;
				AppObjects[AppObjects.size() - 1]->y_m = AppObjects[0]->y_m;
			}
		}
		for (AppObject* object : AppObjects) {
			object->input(e);
		}
	}
}

void editor::editor::Loop()
{
	while (running) {
		//PacketProcess();
		Update();
		Input();
		if (frametime > FrameDelay) {
			framestart = SDL_GetTicks();
			Draw();
		}
		frametime = SDL_GetTicks() - framestart;
		cnt++;
	}
	Cleanup();
}

string editor::PlaceableBounding::PrintReleventData()
{
	return string();
}

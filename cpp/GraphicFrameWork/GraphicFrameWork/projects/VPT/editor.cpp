#include "editor.h"

void editor::editor::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	
	TextBoxData tbd;

	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		AppObjects[i]->id = i;
		cm.AttachComponent(new CollisionBox(AppObjects), AppObjects[i]);
	}
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
				message_m = obj->message;
			}
		}
	}
	for (AppObject* object : AppObjects) {
		object->update();
	}
	//cout << message_m << endl;
}

void editor::editor::Input(bool wasEvent, SDL_Event e)
{
	for (Component* c : cm.UpdateSectorComponents) {
		AppObjects[c->parent_m->id]->collide(c->run(AppObjects));
	}
	if (wasEvent) {
		if (e.window.windowID == windowId) {
			if (e.type == SDL_QUIT || (e.type == SDL_WINDOWEVENT && e.window.event == SDL_WINDOWEVENT_CLOSE)) { running = false; }
			if (e.type == SDL_MOUSEMOTION) {
				AppObjects[0]->x_m = e.motion.x;
				AppObjects[0]->y_m = e.motion.y;
				for (AppObject* object : AppObjects) {
					if (PlaceableBounding* obj = dynamic_cast<PlaceableBounding*>(object)) {
						if (object->id == 1) {
							object->x_m = AppObjects[0]->x_m - obj->selectW;
							object->y_m = AppObjects[0]->y_m - obj->selectH;
						}
					}
				}
			}
			if (e.type == SDL_MOUSEBUTTONUP) {
				AppObjSelected selectedval = Selected();
				if (selectedval.selected == true) {
					AppObjects[selectedval.index]->id = 0;
				}
				else {
					for (unsigned int i = 0; i < AppObjects.size(); i++) {
						if (PlaceableBounding* placeableObj = dynamic_cast<PlaceableBounding*>(AppObjects[i])) {
							if (Utils::contains(AppObjects[i]->getBounds(), SDL_Point{ AppObjects[0]->x_m, AppObjects[0]->y_m })) {
								placeableObj->id = 1;
								placeableObj->selectW = AppObjects[0]->x_m - placeableObj->x_m;
								placeableObj->selectH = AppObjects[0]->y_m - placeableObj->y_m;
								break;
							}
						}
					}
				}
			}
			for (AppObject* object : AppObjects) {
				object->input(e);
			}
		}
	}
	bool objectAdded = false;
	if (message_m == "Dynamic Object") {
		PlaceableBounding* newObj = new PlaceableBounding();
		newObj->PreInit((dir + "bounding.png").c_str());
		newObj->Init(renderer, 100, 100, 400, 400);
		AppObjects.push_back(newObj);
	}
	else if (message_m == "Text") {
		PlaceableText* newObj = new PlaceableText();
		newObj->Init(renderer, 400, 400);
		AppObjects.push_back(newObj);
	} else if (message_m == "Button") {
		PlaceableButton* newObj = new PlaceableButton(dir + "CButtonUP.png", dir + "CButtonP.png");
		newObj->Init(renderer, 100, 100, 400, 400);
		AppObjects.push_back(newObj);
	}
	else if (message_m == "Text Field") {
		PlaceableTextField* newObj = new PlaceableTextField();
		newObj->Init(renderer, 400, 400);
		AppObjects.push_back(newObj);
	}
	else if (message_m == "Simple Button") {
		PlaceableSimpleButton* newObj = new PlaceableSimpleButton();
		newObj->Init(renderer, 400, 400);
		AppObjects.push_back(newObj);
	}
	else if (message_m == "Loading Symbol") {
		PlaceableLoadingSymbol* newObj = new PlaceableLoadingSymbol();
		newObj->Init(renderer, 200, 200, 400, 400);
		AppObjects.push_back(newObj);
	}
	if (objectAdded) {
		AppObjects[AppObjects.size() - 1]->id = 0;
		AppObjects[AppObjects.size() - 1]->x_m = AppObjects[0]->x_m - (AppObjects[AppObjects.size() - 1]->width / 2);
		AppObjects[AppObjects.size() - 1]->y_m = AppObjects[0]->y_m - (AppObjects[AppObjects.size() - 1]->height / 2);
	}
	message_m = "";
}

editor::AppObjSelected editor::editor::Selected()
{
	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		if (PlaceableBounding* obj = dynamic_cast<PlaceableBounding*>(AppObjects[i])) {
			if (AppObjects[i]->id == 1) {
				return AppObjSelected{ true, i};
			}
		}
	}
	return AppObjSelected{false, NULL};
}

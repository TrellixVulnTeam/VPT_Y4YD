#include "client.h"
const string path = "..\\PrinceValiant.ttf";

client::client::client()
{
}

void client::client::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	string img_path = "..\\";
	//init objects here

	//text init
	text = new Text(path, "VPT TEST", SDL_Color{0, 0, 0, 255}, 100);
	text->Init(renderer, 0, 0, 270, 0);
	//text init

	//button init
	//button init

	//tf init
	TextFieldData tfd;
	tf = new TextField(path, tfd.textsize, tfd.x_offset, tfd.y_offset);
	tf->Init(renderer, tfd.w, tfd.h, 270, 160);
	tf->id = 12;
	//tg init

	//AppObjects vector because is need for collision component
	AppObjects.push_back(new AppObject());
	AppObjects[0]->PreInit("");
	AppObjects[0]->Init(renderer, 1, 1, 0, 0);
	//AppObjects vector because is need for collision component

	//init objects here


	//init components here
	cm.AttachComponent(new CollisionBox(AppObjects), tf);
	//init components here
}

void client::client::Draw()
{
	SDL_RenderClear(renderer);
	tf->draw();
	text->draw();
	SDL_RenderPresent(renderer);
}

void client::client::Update()
{
	int UpdateVal;
	tf->update();
	text->update();
	for (unsigned int i = 0; i < cm.UpdateSectorComponents.size(); i++) {
		UpdateVal = cm.UpdateSectorComponents[i]->run(AppObjects);
		//cout << UpdateVal << endl;
		if (cm.UpdateSectorComponents[i]->parent_m->id == 10) {

		}
		if (cm.UpdateSectorComponents[i]->parent_m->id == 12) {
			tf->TextFieldupdate(UpdateVal);
		}
	}
}

void client::client::Input()
{
	for (unsigned int i = 0; i < cm.InputSectorComponents.size(); i++) {
		cm.InputSectorComponents[i]->run(AppObjects);
	}
	if (SDL_PollEvent(&e)) {
		if (e.type == SDL_QUIT) { running = false; }
		if (e.type == SDL_MOUSEMOTION) {
			AppObjects[0]->x_m = e.motion.x;
			AppObjects[0]->y_m = e.motion.y;
		}
		tf->input(e);
	}
	
}

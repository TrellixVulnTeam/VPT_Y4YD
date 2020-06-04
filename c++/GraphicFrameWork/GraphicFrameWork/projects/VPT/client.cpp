#include "client.h"
const string path = "C:\\Users\\richa\\source\\repos\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\HighlandGothicFLF.ttf";

client::client::client()
{
}

void client::client::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	string img_path = "C:\\Users\\richa\\source\\repos\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\";
	//init objects here

	//text init
	text = new Text(path, "Test3", SDL_Color{0,0,0,255}, 60);
	text->Init(renderer, 100, 100, 100, 100);
	//text init

	//button init
	button = new Button("C:\\Users\\richa\\source\\repos\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\button.jpg",
	"C:\\Users\\richa\\source\\repos\\GraphicFrameWork\\GraphicFrameWork\\projects\\VPT\\button1.jpg", nullptr, 0,
	0, 0, 0
	);
	button->Init(renderer, 100, 100, 500, 500);
	button->id = 10;
	//button init

	//AppObjects vector because is need for collision component
	AppObjects.push_back(new AppObject());
	AppObjects[0]->PreInit("");
	AppObjects[0]->Init(renderer, 1, 1, 0, 0);
	//AppObjects vector because is need for collision component

	//init objects here


	//init components here
	cm.AttachComponent(new CollisionBox(AppObjects), button);
	//init components here
}

void client::client::Draw()
{
	SDL_RenderClear(renderer);
	text->draw();
	button->draw();
	SDL_RenderPresent(renderer);
}

void client::client::Update()
{
	int UpdateVal;
	text->update();
	button->update();
	for (unsigned int i = 0; i < cm.UpdateSectorComponents.size(); i++) {
		UpdateVal = cm.UpdateSectorComponents[i]->run(AppObjects);
		//cout << UpdateVal << endl;
		if (cm.UpdateSectorComponents[i]->parent_m->id == 10) {
			button->button_update(UpdateVal);
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
		button->input(e);
	}
	
}

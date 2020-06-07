#include "client.h"
const string path = "..\\PrinceValiant.ttf";
static queue<Packet*> PacketQueue;
static mutex PacketQueueLock;
static JNIEnv* je;

client::client::client()
{
}

void client::client::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	string img_path = "..\\";
	//init objects here

	//AppObjects vector because is need for collision component
	AppObjects.push_back(new AppObject());
	AppObjects[0]->PreInit("");
	AppObjects[0]->Init(renderer, 1, 1, 0, 0);
	//AppObjects vector because is need for collision component

	//text init
	text = new Text(path, "VPT", SDL_Color{ 0, 0, 0, 255 }, 100);
	text->Init(renderer, 0, 0, 270, 0);
	AppObjects.push_back(text);
	//text init

	//button init
	button = new Button("..\\CButtonUP.png", "..\\CButtonP.png", nullptr, 0, 0, 0,0);
	button->Init(renderer, 100, 100, 270, 400);
	AppObjects.push_back(button);
	//button init

	//tf init
	TextFieldData tfd;
	tf = new TextField("Text", path, tfd.textsize, tfd.x_offset, tfd.y_offset);
	tf->Init(renderer, tfd.w, tfd.h, 270, 160);
	AppObjects.push_back(tf);
	tf1 = new TextField("Different Text", path, tfd.textsize, tfd.x_offset, tfd.y_offset, '*');
	tf1->Init(renderer, tfd.w, tfd.h, 270, 270);
	AppObjects.push_back(tf1);
	//tg init

	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		AppObjects[i]->id = i;
	}
	//init objects here


	//init components here
	for (AppObject* object : AppObjects) {
		cm.AttachComponent(new CollisionBox(AppObjects), object);
	}
	//init components here
}

void client::client::Draw()
{
	SDL_RenderClear(renderer);
	for (AppObject* object : AppObjects) {
		object->draw();
	}
	SDL_RenderPresent(renderer);
}

void client::client::Update()
{
	int UpdateVal;
	for (Component* c : cm.UpdateSectorComponents) {
		UpdateVal = c->run(AppObjects);
		AppObject* object1 = AppObjects[c->parent_m->id];
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

void client::client::Input()
{
	for (Component* c: cm.InputSectorComponents) {
		c->run(AppObjects);
	}
	if (SDL_PollEvent(&e)) {
		if (e.type == SDL_QUIT) { running = false; }
		if (e.type == SDL_MOUSEMOTION) {
			AppObjects[0]->x_m = e.motion.x;
			AppObjects[0]->y_m = e.motion.y;
		}
		for (AppObject* object : AppObjects) {
			object->input(e);
		}
	}

}

void client::client::PacketProcess()
{
	Packet* packet = PollPacketQueue();
	if (packet == nullptr) {
		return;
	}
	je->DeleteGlobalRef(packet->packetObj_m);
}

void client::client::Loop()
{
	while (running) {
		PacketProcess();
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

void client::client::SetJNIEnv(JNIEnv* e)
{
	je = e;
}

void client::client::QueuePacket(Packet *p) {
	PacketQueueLock.lock();
	PacketQueue.push(p);
	PacketQueueLock.unlock();
}

Packet* client::client::PollPacketQueue() {
	Packet* out;
	PacketQueueLock.lock();
	if (PacketQueue.empty()) {
		out = nullptr;
	}
	else {
		out = PacketQueue.front();
		PacketQueue.pop();
	}
	PacketQueueLock.unlock();
	return out;
}

#include "client.h"
static queue<Packet*> PacketQueue;
static mutex PacketQueueLock;
static JNIEnv* je;

client::client::client()
{
}
void onclick1() {
	cout << "test" << endl;
}
void client::client::Init(const char* window_title, int w, int h)
{
	BasicInit(window_title, w, h);
	string img_path = dir;
	//init objects here

	//Overlay* overlay = new Overlay(path, "Test Overlay", SDL_Color{ 255, 0, 0, 255 }, SDL_Color{ 255, 255, 255, 255 }, 100, 10, 10, 3000, &Overlays);
	//overlay->Init(renderer, 200, 20);
	//addOverlay(overlay);

	//AppObjects vector because is need for collision component
	AppObjects.push_back(new AppObject());
	AppObjects[0]->PreInit("");
	AppObjects[0]->Init(renderer, 1, 1, 0, 0);
	//AppObjects vector because is need for collision component

	//text init
	text = new Text(fontPath, "VPT", SDL_Color{ 0, 0, 0, 255 }, 100);
	text->Init(renderer, 0, 0, 270, 0);
	AppObjects.push_back(text);
	//text init

	//button init
	button = new Button(dir + "CButtonUP.png", dir + "CButtonP.png", nullptr, 0, 0, 0,0, onclick1);
	button->Init(renderer, 100, 100, 270, 400);
	AppObjects.push_back(button);
	//button init

	//tf init
	TextFieldData tfd;
	tf = new TextField("Text", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset);
	tf->Init(dir, renderer, tfd.w, tfd.h, 270, 160);
	AppObjects.push_back(tf);
	tf1 = new TextField("Different Text", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset, '*');
	tf1->Init(dir, renderer, tfd.w, tfd.h, 270, 270);
	AppObjects.push_back(tf1);
	//tg init

	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		AppObjects[i]->id = i;
		cm.AttachComponent(new CollisionBox(AppObjects), AppObjects[i]);
	}
	//init objects here


	//init components here
	//init components here
}

void client::client::Draw()
{
	SDL_RenderClear(renderer);
	for (AppObject* object : AppObjects) {
		object->draw();
	}
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->draw();
		}
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
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->update();
		}
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
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->input(e);
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

void client::client::addOverlay(AppObject* overlay) {
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

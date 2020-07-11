#include "client.h"
static queue<Packet*> PacketQueue;
static mutex PacketQueueLock;
static mutex PacketRecieveLock;
static condition_variable PacketRecieveNotifier;
static bool isPacketRecieveWaiting = false;
static Packet* recievedPacket;

client::client::client()
{
}
void onclick1(Button* button) {
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
	//AppObjects vector because is need for collision component

	//text init
	text = new Text(fontPath, "VPT", SDL_Color{ 0, 0, 0, 255 }, 100);
	text->Init(renderer, 0, 0, 270, 0);
	//AppObjects.push_back(text);
	//text init

	//button init
	button = new Button(dir + "CButtonUP.png", dir + "CButtonP.png", nullptr, 0, 0, 0,0, [this](Button* button) {onclick1(button); } );
	button->Init(renderer, 100, 100, 270, 400);
	//AppObjects.push_back(button);
	//button init

	//tf init
	TextFieldData tfd;
	tf = new TextField("Text", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset);
	tf->Init(dir, renderer, tfd.w, tfd.h, 270, 160);
	//AppObjects.push_back(tf);
	tf1 = new TextField("Different Text", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset, '*');
	tf1->Init(dir, renderer, tfd.w, tfd.h, 270, 270);
	//AppObjects.push_back(tf1);
	//tg init

	LoadingSymbol* ls = new LoadingSymbol();
	ls->Init(renderer, 100, 100, 100, 400);
	//AppObjects.push_back(ls);

	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		AppObjects[i]->id = i;
		cm.AttachComponent(new CollisionBox(AppObjects), AppObjects[i]);
	}
	BeginLoadingScene(LoginScreen().Create());
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
	GetActiveScene().Draw();
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->draw();
		}
	}
	SDL_RenderPresent(renderer);
}

void client::client::Update()
{
	/*if (SDL_GetTicks() >= 3000 && Overlays.empty()) {
		Overlay* ol = new Overlay(fontPath, "This is it...Nothing else is implemented...", SDL_Color{ 255, 0, 0, 255 }, SDL_Color{ 255, 255, 255, 255 }, 40, 10, 10, 3000, &Overlays);
		ol->Init(windowSize.width, renderer, 20);
		addOverlay(ol);
	}
	if (SDL_GetTicks() >= 9000 && Overlays.size() == 1) {
		Overlay* ol = new Overlay(fontPath, "Seriously...You should probably just close the program...", SDL_Color{ 255, 0, 0, 255 }, SDL_Color{ 255, 255, 255, 255 }, 40, 10, 10, 3000, &Overlays);
		ol->Init(windowSize.width, renderer, 20);
		addOverlay(ol);
	}
	if (SDL_GetTicks() >= 15000 && Overlays.size() == 2) {
		Overlay* ol = new Overlay(fontPath, "Just...Go...", SDL_Color{ 255, 0, 0, 255 }, SDL_Color{ 255, 255, 255, 255 }, 40, 10, 10, 3000, &Overlays);
		ol->Init(windowSize.width, renderer, 20);
		addOverlay(ol);
	}
	if (SDL_GetTicks() >= 21000 && Overlays.size() == 3) {
		Overlay* ol = new Overlay(fontPath, "Goodbye...", SDL_Color{ 255, 0, 0, 255 }, SDL_Color{ 255, 255, 255, 255 }, 40, 10, 10, 3000, &Overlays);
		ol->Init(windowSize.width, renderer, 20);
		addOverlay(ol);
	}
	if (SDL_GetTicks() >= 24000) {
		running = false;
	}*/
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
	GetActiveScene().Update();
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->update();
		}
	}
}

void client::client::Input(bool wasEvent, SDL_Event e)
{
	for (Component* c: cm.InputSectorComponents) {
		c->run(AppObjects);
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
			GetActiveScene().Input(e);
			for (AppObject* overlay : Overlays) {
				if (overlay != nullptr) {
					overlay->input(e);
				}
			}
			for (AppObject* overlay : Overlays) {
				if (overlay != nullptr) {
					overlay->input(e);
				}
			}
		}
	}
}

void client::client::PacketProcess()
{
	Packet* packet = PollPacketQueue();
	if (packet == nullptr) {
		return;
	}
	if (packet->packetId_m == PacketId_FORCE_LOGOUT) {
		//TODO: Do Something
		delete packet;
		return;
	}
	if (isPacketRecieveWaiting) {
		lock_guard<mutex> prwlg(PacketRecieveLock);
		recievedPacket = packet;
		isPacketRecieveWaiting = false;
		PacketRecieveNotifier.notify_one();
	}
}

void client::client::Loop()
{
	while (running) {
#ifndef USE_DEBUGGER
		try {
#endif
			SDL_Event e;
			bool wasEvent = SDL_PollEvent(&e);
			PacketProcess();
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

void client::client::QueuePacket(Packet *p) {
	lock_guard<mutex> pqlg(PacketQueueLock);
	PacketQueue.push(p);
}

Packet* client::client::PollPacketQueue() {
	Packet* out;
	lock_guard<mutex> pqlg(PacketQueueLock);
	if (PacketQueue.empty()) {
		out = nullptr;
	}
	else {
		out = PacketQueue.front();
		PacketQueue.pop();
	}
	return out;
}

void client::client::sendPacket(jobject packet) {
	JNIEnv* env = Env::GetJNIEnv();
	jclass methodClass = env->FindClass("client/ClientMain");
	jmethodID sendMethodId = env->GetStaticMethodID(methodClass, "sendPacket", "(Lcommon/networking/packet/Packet;)V");
	env->CallStaticVoidMethod(methodClass, sendMethodId, packet);
}

Packet* client::client::Request(jobject packet) {
	unique_lock<mutex> prwlg(PacketRecieveLock);
	isPacketRecieveWaiting = true;
	sendPacket(packet);
	PacketRecieveNotifier.wait(prwlg);
	Packet* out = recievedPacket;
	isPacketRecieveWaiting = false;
	return out;
}
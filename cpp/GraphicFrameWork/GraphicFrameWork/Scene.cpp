#include "Scene.h"

static int nextId = 0;

Scene::Scene() : id(nextId) { nextId++;  }

void Scene::Init(AppInstance* instance) {
	Scene::instance = instance;
	Objects.push_back(new AppObject());
	Objects[0]->PreInit("");
	Objects[0]->Init(instance->renderer, 1, 1, 0, 0);
	LoadStaticComponents();
	LoadComponents();
	AttachListeners();
	PostInit();
	if (doneInitOnInit) {
		instance->FinishSceneLoading(*this);
	}
}

void Scene::AttachListeners() {
	for (unsigned int i = 0; i < Objects.size(); i++) {
		Objects[i]->id = i;
		if (needsCollisions) {
			cm.AttachComponent(new CollisionBox(Objects), Objects[i]);
		}
	}
}

void Scene::Update() {
	if(needsCollisions) {
		int UpdateVal;
		for (Component* c : cm.UpdateSectorComponents) {
			UpdateVal = c->run(Objects);
			AppObject* object1 = Objects[c->parent_m->id];
			if (TextField* TextFieldObj = dynamic_cast<TextField*>(object1)) {
				SDL_StartTextInput();
			}
			object1->collide(UpdateVal);
		}
	}
	for (AppObject* object : Objects) {
		object->update();
	}
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->update();
		}
	}
}

void Scene::Input(SDL_Event e) {
	for (Component* c : cm.InputSectorComponents) {
		c->run(Objects);
	}
	if (e.type == SDL_MOUSEMOTION) {
		Objects[0]->x_m = e.motion.x;
		Objects[0]->y_m = e.motion.y;
	}
	for (AppObject* object : Objects) {
		object->input(e);
	}
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->input(e);
		}
	}
}

void Scene::Draw() {
	for (AppObject* object : Objects) {
		object->draw();
	}
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->draw();
		}
	}
}

map<string, Scene*>* StaticScene::staticScenes = new map<string, Scene*>();

Scene& StaticScene::StaticCreate(Scene& classRef) {
	string refName = typeid(classRef).name();
	if (staticScenes->count(refName) != 1) {
		staticScenes->insert(make_pair(refName, &DoCreate()));
	}
	return (*staticScenes->find(refName)->second);
}

void DynamicScene::PostInit() {
	BeginLoadingDynamicComponents();
	PostInitD();
}

Text* LoadingScreen::text = nullptr;
LoadingSymbol* LoadingScreen::loadingSymbol = nullptr;

void LoadingScreen::LoadStaticComponents() {
	if (text == nullptr) {

		text = new Text(fontPath, "Loading...", SDL_Color{ 0, 0, 0, 255 }, 100);
		int textWidth, textHeight;
		TTF_SizeText(text->font_m, text->message.c_str(), &textWidth, &textHeight);
		text->Init(instance->renderer, 0, 0, (instance->windowSize.width - textWidth) / 2, 50);

		loadingSymbol = new LoadingSymbol();
		loadingSymbol->Init(instance->renderer, 200, 200, (instance->windowSize.width - 200) / 2, textHeight + 100);
		textSpeed = (3 / (360 / loadingSymbol->rotationSpeed_m)) / 1.5;

	}
	Objects.push_back(text);
	Objects.push_back(loadingSymbol);
}

void LoadingScreen::Update() {
	string newMsg = "Loading";
	for (int i = 0; i < (int)(textSpeed * SDL_GetTicks()) % 4; i++) {
		newMsg += ".";
	}
	text->ChangeText(newMsg);
	int textWidth;
	TTF_SizeText(text->font_m, text->message.c_str(), &textWidth, NULL);
	text->x_m = (instance->windowSize.width - textWidth) / 2;
	Scene::Update();
}

Text* LoginScreen::text = nullptr;
SimpleButton* LoginScreen::loginButton = nullptr;

void LoginScreen::LoadStaticComponents() {
	if (text == nullptr) {

		text = new Text(fontPath, "VPT Login", SDL_Color{ 0, 0, 0, 255 }, 100);
		int textWidth, textHeight;
		TTF_SizeText(text->font_m, text->message.c_str(), &textWidth, &textHeight);
		text->Init(instance->renderer, 0, 0, (instance->windowSize.width - textWidth) / 2, 30);
		
		loginButton = new SimpleButton(new Text(fontPath, "Login", SDL_Color{ 0, 0, 0, 255 }, 30), 10, 10, [this]( SimpleButton* button ) {login(); });
		loginButton->Init(instance->renderer, 270, 400);

	}
	Objects.push_back(text);
	Objects.push_back(loginButton);
}

void LoginScreen::LoadComponents() {

	client::TextFieldData tfd;

	usernameField = new TextField("Username", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset);
	usernameField->Init(dir, instance->renderer, tfd.w, tfd.h, 270, 160);

	passwordField = new TextField("Password", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset, '*');
	passwordField->Init(dir, instance->renderer, tfd.w, tfd.h, 270, 270);

	Objects.push_back(usernameField);
	Objects.push_back(passwordField);

}

void LoginScreen::login() {
	thread([this]() {doLogin(); Env::DetachCurrentThread(); }).detach();
}

void LoginScreen::doLogin() {
	instance->isDisplayingScene = false;
	string username = usernameField->message;
	string password = passwordField->message;

	JNIEnv* env = Env::GetJNIEnv();
	env->PushLocalFrame(2);
	env->PushLocalFrame(3);
	jclass stringClass = env->FindClass("java/lang/String");
	jmethodID getBytesMethodId = env->GetMethodID(stringClass, "getBytes", "()[B");
	jbyteArray passwordBytes = (jbyteArray)env->CallObjectMethod(env->NewStringUTF(password.c_str()), getBytesMethodId);
	jclass loginPacketClass = env->FindClass("common/networking/packet/packets/LoginPacket");
	jmethodID constructorId = env->GetMethodID(loginPacketClass, "<init>", "(Ljava/lang/String;[B)V");
	jobject loginPacket = env->NewObject(loginPacketClass, constructorId, env->NewStringUTF(username.c_str()), passwordBytes);

	Packet* loginResult = client::client::Request(env->PopLocalFrame(loginPacket));

	bool isValidPacket = Utils::ValidatePacketType(loginResult->resultType_m, ResultId_STANDARD_RESULT);

	bool wasSuccessful;
	string message;

	if (isValidPacket) {
		jclass resultPacketClass = env->FindClass("common/networking/packet/packets/result/ResultPacket");
		jfieldID successFieldId = env->GetFieldID(resultPacketClass, "wasActionSuccessful", "Z");
		wasSuccessful = env->GetBooleanField(loginResult->packetObj_m, successFieldId) == JNI_TRUE;
		if (wasSuccessful) {
			message = "Success! Logged In!";
		}
		else {
			jfieldID messageFieldId = env->GetFieldID(resultPacketClass, "msg", "Ljava/lang/String;");
			message = string(env->GetStringUTFChars((jstring)env->GetObjectField(loginResult->packetObj_m, messageFieldId), NULL));
		}
	}
	else {
		wasSuccessful = false;
		message = "Invalid Responce From Server Please Try Again";
	}

	env->PopLocalFrame(NULL);

	delete loginResult;

	getProfilePic();

	instance->RequestSDLFunct([this, message, wasSuccessful]() {
		Overlay* resultOverlay = new Overlay(fontPath, message, wasSuccessful ? SDL_Color{ 0, 255, 0, 255 } : SDL_Color{ 255, 0, 0, 255 }, SDL_Color{ 255, 255, 255, 255 }, 40, 10, 10, 3000, &instance->Overlays);
		resultOverlay->Init(instance->windowSize.width, instance->renderer, 20);
		instance->addOverlay(resultOverlay);
		instance->isDisplayingScene = true;
	});
}

void StandardQuestion::LoadStaticComponents()
{
}

void StandardQuestion::LoadComponents()
{
	int txbx = 270;
	int ptx = 360;
	int Sbutton_offset = 90;
	prompt = new Text(fontPath, question_m, SDL_Color{ 0,0,0, 255 }, 40);
	prompt->Init(instance->renderer, 0, 0, ptx, 0);

	client::TextFieldData tfd;
	answer_box = new TextField("Answer", fontPath, tfd.textsize, tfd.x_offset, tfd.y_offset);
	answer_box->Init(dir, instance->renderer, tfd.w, tfd.h, txbx, 100);


	Monoca_Editor_open = new SimpleButton(new Text(fontPath, "Open Editor", SDL_Color{ 0, 0,0, 255 }, 30), 10, 10, [this](SimpleButton* button) {onClickM(button); });
	Monoca_Editor_open->Init(instance->renderer, ptx - Sbutton_offset, 210);

	Esc_Menu_open = new SimpleButton(new Text(fontPath, "Open Test Menu", SDL_Color{ 0, 0,0, 255 }, 30), 10, 10, [this](SimpleButton* button) {onClickEM(button); });
	Esc_Menu_open->Init(instance->renderer, ptx - Sbutton_offset, 280);

	Objects.push_back(prompt);
	Objects.push_back(answer_box);
	Objects.push_back(Monoca_Editor_open); 
	Objects.push_back(Esc_Menu_open);
}

void StandardQuestion::Input(SDL_Event e)
{
	for (Component* c : cm.InputSectorComponents) {
		c->run(Objects);
	}
	if (e.type == SDL_MOUSEMOTION) {
		Objects[0]->x_m = e.motion.x;
		Objects[0]->y_m = e.motion.y;
	}
	if (e.type == SDL_KEYUP) {
		if (e.key.keysym.sym == SDLK_ESCAPE) {
			cout << "worked" << endl;
			GetInput = "esc_menu";
		}
	}
	for (AppObject* object : Objects) {
		object->input(e);
	}
	for (AppObject* overlay : Overlays) {
		if (overlay != nullptr) {
			overlay->input(e);
		}
	}
}

void StandardQuestion::onClickM(SimpleButton* sb)
{
	CppPython::ExecPython("app");
}

void StandardQuestion::onClickEM(SimpleButton* sb)
{
	cout << "worked" << endl;
	GetInput = "esc_menu";
}

void LoginScreen::Draw() {
	Scene::Draw();
	if(userIcon != nullptr)
		SDL_RenderCopy(instance->renderer, userIcon, NULL, new SDL_Rect{ 256, 256, 256, 256 });
}

void LoginScreen::getProfilePic() {
	JNIEnv* env = Env::GetJNIEnv();
	env->PushLocalFrame(4);
	env->PushLocalFrame(1);
	jclass packetClass = env->FindClass("common/networking/packet/Packet");
	jmethodID constructorId = env->GetMethodID(packetClass, "<init>", "(I)V");
	jobject requestPacket = env->NewObject(packetClass, constructorId, PacketId_CURRENT_USER_REQUEST);

	Packet* resultPacket = client::client::Request(env->PopLocalFrame(requestPacket));
	if (Utils::ValidatePacketType(resultPacket->resultType_m, ResultId_USER_RESULT)) {
		jclass resultPacketClass = env->FindClass("common/networking/packet/packets/result/ResultPacket");
		jfieldID successFieldId = env->GetFieldID(resultPacketClass, "wasActionSuccessful", "Z");
		if (env->GetBooleanField(resultPacket->packetObj_m, successFieldId) == JNI_TRUE) {
			jclass singleResultPacketClass = env->FindClass("common/networking/packet/packets/result/SingleResultPacket");
			jfieldID dataFieldId = env->GetFieldID(singleResultPacketClass, "result", "Ljava/lang/Object;");
			jobject user = env->GetObjectField(resultPacket->packetObj_m, dataFieldId);
			if (user == NULL) {
				cerr << "Null User" << endl;
			}
			else {
				jclass netUserClass = env->FindClass("common/user/NetPublicUser");
				jmethodID attributesMethod = env->GetMethodID(netUserClass, "getAttributes", "()Ljava/util/ArrayList;");
				jobject attributes = env->CallObjectMethod(user, attributesMethod);
				jclass attributeTypeClass = env->FindClass("common/user/UserAttributeType");
				jfieldID requestedAttributeTypeFieldId = env->GetStaticFieldID(attributeTypeClass, "USERICON", "Lcommon/user/UserAttributeType;");
				jobject requestedAttributeType = env->GetStaticObjectField(attributeTypeClass, requestedAttributeTypeFieldId);
				jclass attributeClass = env->FindClass("common/user/NetUserAttribute");
				jfieldID typeFieldId = env->GetFieldID(attributeClass, "type", "Lcommon/user/UserAttributeType;");

				jclass arrayListClass = env->FindClass("java/util/ArrayList");
				jmethodID arrayListSizeMethodId = env->GetMethodID(arrayListClass, "size", "()I");
				jint arrayListSize = env->CallIntMethod(attributes, arrayListSizeMethodId);
				jmethodID arrayListGetMethodId = env->GetMethodID(arrayListClass, "get", "(I)Ljava/lang/Object;");

				for (jint i = 0; i < arrayListSize; i++) {
					jobject attr = env->CallObjectMethod(attributes, arrayListGetMethodId, i);
					if (env->IsSameObject(requestedAttributeType, env->GetObjectField(attr, typeFieldId))) {
						jclass netSingleDataAttributeClass = env->FindClass("common/user/NetSingleDataAttribute");
						jmethodID getMethodId = env->GetMethodID(netSingleDataAttributeClass, "getData", "()Ljava/lang/Object;");
						jobject image = env->CallObjectMethod(attr, getMethodId);
						jobject globalImageRef = env->NewGlobalRef(image);
						instance->RequestSDLFunct([this, globalImageRef]() {
							userIcon = Utils::CreateTextureFromImage(globalImageRef, instance->renderer, Env::GetJNIEnv());
							Env::GetJNIEnv()->DeleteGlobalRef(globalImageRef);
						});
						break;
					}
					env->DeleteLocalRef(attr);
				}
			}
		}
		else {
			jfieldID messageFieldId = env->GetFieldID(resultPacketClass, "msg", "Ljava/lang/String;");
			cerr << string(env->GetStringUTFChars((jstring)env->GetObjectField(resultPacket->packetObj_m, messageFieldId), NULL)) << endl;
		}
	}
	else {
		cerr << "Invalid Responce From Server Please Try Again" << endl;
	}

	env->PopLocalFrame(NULL);
}

void escMenu::LoadStaticComponents()
{
}

void escMenu::LoadComponents()
{
	int ptx = 370;
	Title = new Text(fontPath, "Test Menu", SDL_Color{0,0,0,255}, 70);
	Title->Init(instance->renderer, 0, 0, ptx, 0);

	ResumeTest = new SimpleButton(new Text(fontPath, "Resume", SDL_Color{ 0,0,0,255 }, 50), 10, 10, [this](SimpleButton* button) { });
	ResumeTest->Init(instance->renderer, ptx + 60, 130);

	options = new SimpleButton(new Text(fontPath, "Options", SDL_Color{ 0,0,0,255 }, 50), 10, 10, [this](SimpleButton* button) {});
	options->Init(instance->renderer, ptx + 60, 270);

	Exit = new SimpleButton(new Text(fontPath, "Save and Quit", SDL_Color{ 0,0,0,255 }, 50), 10, 10, [this](SimpleButton* button) {});
	Exit->Init(instance->renderer, ptx, 410);

	Objects.push_back(Title);
	Objects.push_back(ResumeTest);
	Objects.push_back(options);
	Objects.push_back(Exit);
}

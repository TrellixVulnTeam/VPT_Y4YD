#include "Utils.h"
static map<SDL_Renderer*, vector<Utils::SDL_RendererState>*> states;
void Utils::SDL_PushRendererState(SDL_Renderer* renderer) {
	if (renderer == nullptr) {
		return;
	}
	if (!contains(states, renderer)) {
		states.insert(make_pair(renderer, new vector<SDL_RendererState>()));
	}
	get(states, renderer)->push_back(SDL_GetRendererState(renderer));
}

void Utils::SDL_PopRendererState(SDL_Renderer* renderer) {
	if (renderer == nullptr) {
		return;
	}
	if (!contains(states, renderer)) {
		return;
	}
	vector<SDL_RendererState>* rendererStates = get(states, renderer);
	if (rendererStates->empty()) {
		return;
	}
	SDL_SetRendererState(renderer, rendererStates->back());
	rendererStates->pop_back();
}

Utils::SDL_RendererState Utils::SDL_GetRendererState(SDL_Renderer* renderer) {
	if (renderer == nullptr) {
		return SDL_RendererState{ SDL_Color{0, 0, 0, 0} };
	}
	return SDL_RendererState{ SDL_GetRenderDrawSDLColor(renderer) };
}

void Utils::SDL_SetRendererState(SDL_Renderer* renderer, SDL_RendererState state) {
	if (renderer == nullptr) {
		return;
	}
	SDL_SetRenderDrawSDLColor(renderer, state.color);
}

SDL_Color Utils::SDL_GetRenderDrawSDLColor(SDL_Renderer* renderer) {
	if (renderer == nullptr) {
		return SDL_Color{ 0, 0, 0, 0 };
	}
	SDL_Color out;
	SDL_GetRenderDrawColor(renderer, &out.r, &out.g, &out.b, &out.a);
	return out;
}

void Utils::SDL_SetRenderDrawSDLColor(SDL_Renderer* renderer, SDL_Color color) {
	if (renderer == nullptr) {
		return;
	}
	SDL_SetRenderDrawColor(renderer, color.r, color.g, color.b, color.a);
}

string Utils::readClipboard() {
	if (!IsClipboardFormatAvailable(CF_TEXT) || !OpenClipboard(nullptr)) {
		return string();
	}
	HANDLE hData = GetClipboardData(CF_TEXT);
	if (hData == nullptr) {
		CloseClipboard();
		return string();
	}
	char* text = static_cast<char*>(GlobalLock(hData));
	if (text == nullptr) {
		CloseClipboard();
		return string();
	}
	string out(text);
	GlobalUnlock(hData);
	CloseClipboard();
	return out;
}

void Utils::writeClipboard(string data) {
	if (!OpenClipboard(nullptr)) {
		return;
	}
	EmptyClipboard();
	HGLOBAL textMemObj = GlobalAlloc(GMEM_MOVEABLE, data.size()+1);
	if (textMemObj == NULL) {
		CloseClipboard();
		return;
	}
	LPSTR copy = static_cast<LPSTR>(GlobalLock(textMemObj));
	if (copy == NULL) {
		CloseClipboard();
		return;
	}
	memcpy(copy, data.c_str(), data.size()+1);
	GlobalUnlock(textMemObj);
	SetClipboardData(CF_TEXT, textMemObj);
	CloseClipboard();
}

bool Utils::ValidatePacketType(int packetType, int expectedType) {
	if (packetType == expectedType) {
		return true;
	}
	switch (packetType) {
	case ResultId_ILLEGAL_ACCESS:
	case ResultId_INVALID_REQUEST:
	case ResultId_SERVER_ERROR:
	case ResultId_TOO_MANY_REQUESTS:
		return true;
	default:
		return false;
	}
}

#pragma warning(push)
#pragma warning(disable:6385)
#pragma warning(disable:6386)
SDL_Texture* Utils::CreateTextureFromImage(jobject image, SDL_Renderer* renderer, JNIEnv* env) {
	env->PushLocalFrame(1);

	jclass imageClass = env->FindClass("common/SerializableImage");
	jmethodID dataRetrievalMessageId = env->GetMethodID(imageClass, "exportToSDL", "()[I");
	jintArray rawImageData = (jintArray)env->CallObjectMethod(image, dataRetrievalMessageId);

	jsize dataSize = env->GetArrayLength(rawImageData);
	jint* rawImageDataArr = env->GetIntArrayElements(rawImageData, nullptr);
	Uint32* imageData = new Uint32[dataSize];
	memcpy(imageData, rawImageDataArr, dataSize * sizeof(Uint32));
	env->ReleaseIntArrayElements(rawImageData, rawImageDataArr, 0);
	int width = imageData[0];
	int height = imageData[1];
	
	SDL_Texture* texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888, SDL_TEXTUREACCESS_STATIC, width, height);
	Uint32* pixels = new Uint32[(size_t)dataSize-2];
	
	memcpy(pixels, &imageData[2], ((size_t)dataSize - 2) * sizeof(Uint32));

	SDL_UpdateTexture(texture, NULL, pixels, width * sizeof(Uint32));

	delete[] imageData;
	delete[] pixels;

	env->PopLocalFrame(NULL);

	return texture;
}
#pragma warning(pop)

bool Utils::contains(SDL_Rect* rect, SDL_Point point) {
	return rect->x <= point.x && rect->x + rect->w >= point.x && rect->y <= point.y && rect->y + rect->h >= point.y;
}

wstring Utils::FromString(string str) {
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	return converter.from_bytes(str);
}

wchar_t* Utils::UnConstWChar(const wchar_t* chars) {
	wchar_t* out = new wchar_t[sizeof(chars) / sizeof(wchar_t)];
	memcpy(out, chars, sizeof(chars));
	return out;
}
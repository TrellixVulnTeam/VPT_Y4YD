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

template<typename kt, typename vt>
bool Utils::contains(map<kt, vt> m, kt key) {
	return m.count(key) == 1;
}

template<typename kt, typename vt>
vt Utils::get(map<kt, vt> m, kt key) {
	auto ittr = m.find(key);
	if (ittr == m.end()) {
		return NULL;
	}
	return ittr->second;
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
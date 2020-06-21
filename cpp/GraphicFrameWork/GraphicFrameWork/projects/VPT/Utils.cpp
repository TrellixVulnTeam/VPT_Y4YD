#include "Utils.h"
static map<SDL_Renderer*, vector<Utils::SDL_RendererState>*> states;
void Utils::SDL_PushRendererState(SDL_Renderer* renderer) {
	if (renderer == nullptr) {
		return;
	}
	if (!contains(states, renderer)) {
		states.insert(pair<SDL_Renderer*, vector<SDL_RendererState>*>(renderer, new vector<SDL_RendererState>()));
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
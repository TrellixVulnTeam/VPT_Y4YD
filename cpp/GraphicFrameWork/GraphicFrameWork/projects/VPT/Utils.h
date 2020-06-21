#pragma once
#include <SDL.h>
#include <map>
#include <vector>
using namespace std;
class Utils {
public:
	static struct SDL_RendererState {
		SDL_Color color;
	};
	static void SDL_PushRendererState(SDL_Renderer* renderer);
	static void SDL_PopRendererState(SDL_Renderer* renderer);
	static SDL_RendererState SDL_GetRendererState(SDL_Renderer* renderer);
	static void SDL_SetRendererState(SDL_Renderer* renderer, SDL_RendererState state);
	static SDL_Color SDL_GetRenderDrawSDLColor(SDL_Renderer* renderer);
	static void SDL_SetRenderDrawSDLColor(SDL_Renderer* renderer, SDL_Color color);
	template<typename kt, typename vt>
	static bool contains(map<kt, vt> m, kt key);
	template<typename kt, typename vt>
	static vt get(map<kt, vt> m, kt key);
};
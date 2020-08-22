function compose_settings_packet(meth, data){
	if(meth === "send"){
		return {
			request: "get-module",
			sdata: "data",
			module: "meditor",
			module_request: "send-settings"
			settings: data
		};
	}
	if(meth === "get"){

		return {
			request: "get-module",
			sdata: "data",
			module: "meditor",
			module_request: "settings"
		};

	}
}

function compose_code_packet(code){

}
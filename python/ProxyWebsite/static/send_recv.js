function SRPacket(packet, func){
	fetch(`${window.origin}/recv_packet`, {
		method: "POST",
		body: JSON.stringify(packet),
		cache: "no-cache",
		headers: new Headers({
			"content-type": "application/json"
		})
	})
	.then( (response)=>{
		if(response.ok){
			return response.json();
		}else{
			return;
		}
	})
	.then( (data)=>{
		if(packet.sdata === "data"){
			func(data.sdata)
		}
		if(packet.sdata === "obj_data"){
			func(data)
		}
	})
}

import React, {  Component } from 'react';

class SearchBar extends Component{

    insertAtCareto(el, dele, value) {
        if (el.selectionStart || el.selectionStart == "0") {
            var start = el.selectionStart;
            var end = el.selectionEnd;
            var val_start = el.value.substring(0, start-dele);
            var val_end = el.value.substring(end, el.value.length);
            el.value = val_start + value + val_end;
            return (val_start + value).length;
    	} else {
            el.value += value;
            return el.value.length;
    	}
    }

    setCaretPositiono(ctrl, pos) {
        if (ctrl.setSelectionRange) {
            ctrl.focus();
            ctrl.setSelectionRange(pos, pos);
        } else if (ctrl.createTextRange) {
            var range = ctrl.createTextRange();
            range.collapse(true);
            range.moveEnd('character', pos);
            range.moveStart('character', pos);
            range.select();
        }
    }


    handleKeyPress(e) {
        var x = (window.event) ? e.keyCode : e.which;
        console.log("pressed");

        if(ctrlPressed || !banglaMode)
            x = -1;

        if((x > 64 && x < 91) || (x > 96 && x < 123)) {
            writing += String.fromCharCode(e.which);
            var bangla = parser.toBangla(writing);
            var p = insertAtCareto(elem, phLen, bangla);
            setCaretPositiono(elem, p);
            phLen = bangla.length;
            return false;
        }else {
            writing = "";
            phLen = 0;
        }
        return true;
    }

    handleKeyDown(e) {
        var x = (window.event) ? e.keyCode : e.which;
        if(x == 17){
            ctrlPressed = true;
            return true;
        }
        // for chrome, when control is pressed, other keys go through keydown
        // and for firefox it goes through both keydown & keypress
        if((x == 109 || x == 77) && ctrlPressed){
            banglaMode = !banglaMode;
            writing = "";
            phLen = 0;
        }

    }

    handleKeyUp(e){
        var x = (window.event) ? e.keyCode : e.which;
        if(x == 17){
            ctrlPressed = false;
        }else if(x == 8){ // for chrome, backspace is not in keypress event
            writing = "";
            phLen = 0;
        }
    }

    constructor(props) {
        super(props);
        this.state = {
            searchTerm: ''
        };
        var parser = new RidmikParser();
        var writing = "";
        var phLen = 0;
        var banglaMode = true;
        var ctrlPressed = false;
    }

    onInputChange(searchTerm){
        this.setState({searchTerm});
        this.props.onSearchTermChange(searchTerm);
    }

    render() {
        return (
            <div>
                <input type="text"
                    value={this.state.searchTerm}
                    onChange={e => this.onInputChange(e.target.value)}
                    onKeyPress={e => this.handleKeyPress(e)}
                    onKeyDown={e => this.handleKeyDown(e)}
                    onKeyUp={e => this.handleKeyUp(e)}

                />
                <h5>{this.state.searchTerm}</h5>
            </div>
        );
    }
}


export default SearchBar;

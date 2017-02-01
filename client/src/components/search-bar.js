import React, {  Component } from 'react';

class SearchBar extends Component{

    constructor(props) {
        super(props);
        this.state = {
            searchTerm: ''
        };
    }

    onInputChange(searchTerm){
        this.setState({searchTerm});
        this.props.onSearchTermChange(searchTerm);
    }

    render() {
        return (
            <div>
                <input
                    value={this.state.searchTerm}
                    onChange={e => this.onInputChange(e.target.value)}
                />
                <h5>{this.state.searchTerm}</h5>
            </div>
        );
    }
}


export default SearchBar;
